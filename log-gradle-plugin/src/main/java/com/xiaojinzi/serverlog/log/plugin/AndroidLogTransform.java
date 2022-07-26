package com.xiaojinzi.serverlog.log.plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.utils.FileUtils;

import org.gradle.api.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

public class AndroidLogTransform extends BaseTransform {

    public AndroidLogTransform(Project project) {
    }

    @Override
    public String getName() {
        return "AndroidLogPlugin";
    }

    /**
     * okhttp3.OkHttpClient#interceptors
     */
    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);

        // 消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        // OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();

        try {
            for (TransformInput input : inputs) {
                for (JarInput jarInput : input.getJarInputs()) {

                    File dest = outputProvider.getContentLocation(
                            jarInput.getFile().getAbsolutePath(),
                            jarInput.getContentTypes(),
                            jarInput.getScopes(),
                            Format.JAR);

                    File destJarFile = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString() + "_" + jarInput.getFile().getName());
                    if (destJarFile.exists()) {
                        destJarFile.delete();
                    }

                    JarFile jarFile = new JarFile(jarInput.getFile());
                    Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();

                    JarOutputStream jarOutputStream = new JarOutputStream(
                            new FileOutputStream(destJarFile)
                    );
                    while (jarEntryEnumeration.hasMoreElements()) {
                        JarEntry jarEntry = jarEntryEnumeration.nextElement();
                        String entryName = jarEntry.getName();
                        // System.out.println("entryName = " + entryName);
                        if (entryName != null
                                && !entryName.startsWith("META-INF/")
                                && !entryName.endsWith("module-info.class")
                                && entryName.endsWith(".class")
                                && !entryName.equals("com/xiaojinzi/develop/AndroidLog.class")) {
                            InputStream inputStream = jarFile.getInputStream(new ZipEntry(jarEntry));
                            byte[] bytes = AndroidLogModify.doModify(inputStream);
                            ZipEntry okHttpClientZipEntry = new ZipEntry(jarEntry.getName());
                            okHttpClientZipEntry.setSize(bytes.length);
                            CRC32 crc = new CRC32();
                            crc.update(bytes);
                            okHttpClientZipEntry.setCrc(crc.getValue());
                            jarOutputStream.putNextEntry(okHttpClientZipEntry);
                            jarOutputStream.write(bytes);
                            inputStream.close();
                        } else {
                            ZipEntry zipEntry = new ZipEntry(jarEntry);
                            zipEntry.setCompressedSize(-1);
                            jarOutputStream.putNextEntry(zipEntry);
                            InputStream inputStream = jarFile.getInputStream(zipEntry);
                            IOUtil.readAndWrite(inputStream, jarOutputStream);
                            inputStream.close();
                        }
                        jarOutputStream.closeEntry();
                    }
                    jarOutputStream.close();
                    FileUtils.copyFile(destJarFile, dest);
                    // 删除文件
                    destJarFile.delete();
                }
                for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                    File dest = outputProvider.getContentLocation(directoryInput.getName(),
                            directoryInput.getContentTypes(), directoryInput.getScopes(),
                            Format.DIRECTORY);
                    File directoryInputFile = directoryInput.getFile();
                    recursiveFile(directoryInputFile, file -> {
                        String filePath = file.getPath();
                        // System.out.println("entryName = " + filePath);
                        if (file.isFile()
                                && file.exists()
                                && !filePath.endsWith("module-info.class")
                                && filePath.endsWith(".class")
                                && !filePath.endsWith("com/xiaojinzi/develop/AndroidLog.class")) {
                            FileInputStream inputStream = new FileInputStream(file);
                            byte[] bytes = AndroidLogModify.doModify(inputStream);
                            inputStream.close();
                            IOUtil.byteArrayToFile(file, bytes);
                        }
                    });
                    //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
                    FileUtils.copyDirectory(directoryInputFile, dest);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new IOException(e);
        }

    }

    private void recursiveFile(File file, IFileRecursive fileRecursive) throws Exception {
        if (file.isFile()) {
            fileRecursive.accept(file);
        } else {
            File[] files = file.listFiles();
            for (File subFile : files) {
                if (subFile.isFile()) {
                    fileRecursive.accept(subFile);
                } else {
                    recursiveFile(subFile, fileRecursive);
                }
            }
        }
    }

}
