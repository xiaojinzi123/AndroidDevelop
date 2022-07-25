package com.xiaojinzi.click.debounce.plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class ViewSetOnClickListenerTransform extends BaseTransform {

  public ViewSetOnClickListenerTransform() {
  }

  @Override
  public String getName() {
    return "AndroidViewClickDebouncePlugin";
  }

  /**
   * android.view.View
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
            // 如果是目标工具类, 就换成手动生成的
            if ("android/view/View.class".equals(entryName)) {
              System.out.println("找到了---------");
            }
            if (entryName.contains("View.class")) {
              System.out.println("找到了---------View:" + entryName);
            }
            if ("okhttp3/OkHttpClient.class".equals(entryName)) {
              System.out.println("找到了---------OkHttpClient");
            }

            ZipEntry zipEntry = new ZipEntry(jarEntry);
            zipEntry.setCompressedSize(-1);
            jarOutputStream.putNextEntry(zipEntry);
            InputStream inputStream = jarFile.getInputStream(zipEntry);
            IOUtil.readAndWrite(inputStream, jarOutputStream);
            inputStream.close();
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
          //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
          FileUtils.copyDirectory(directoryInputFile, dest);
        }
      }
    } catch (Exception e) {
      e.printStackTrace(System.out);
      throw new IOException(e);
    }

  }

}
