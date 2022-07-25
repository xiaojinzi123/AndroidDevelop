package com.xiaojinzi.serverlog.log.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface IFileRecursive {

    void accept(File file) throws Exception;

}
