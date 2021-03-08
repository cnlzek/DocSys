package com.DocSystem.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.springframework.web.multipart.MultipartFile;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;
import util.DateFormat;
import util.ReturnAjax;
import util.Encrypt.Base64File;
import util.FileUtil.CompressPic;

public class FileUtil {
    //自动检测文件字符编辑
	public static boolean saveDocContentToFile(String content, String path, String name) {
		String filePath = path + name;
		String encode = getCharset(filePath);
		if(encode == null)
		{
			encode = "UTF-8";
		}
		return  saveDocContentToFile(content, path, name, encode);
	}
    
	//使用指定或系统默认字符编码
	public static boolean saveDocContentToFile(String content, String path, String name,  String encode)
	{	
		System.out.println("saveDocContentToFile() encode:" + encode);
		if(content == null)
		{
			System.out.println("saveDocContentToFile() content is null");
			return false;
		}
		
		File folder = new File(path);
		if(!folder.exists())
		{
			//System.out.println("saveDocContentToFile() path:" + path + " not exists!");
			if(folder.mkdirs() == false)
			{
				System.out.println("saveDocContentToFile() mkdir path:" + path + " Failed!");
				return false;
			}
		}
		
		//创建文件输入流
		String filePath = path + name;
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
		} catch (FileNotFoundException e) {
			System.out.println("saveVirtualDocContent() new FileOutputStream failed");
			e.printStackTrace();
			return false;
		}
		try {
			byte[] buff = null;
			System.out.println("saveDocContentToFile " +path+ " encode:" + encode);	

			if(encode == null)
			{
				buff = content.getBytes();
			}
			else
			{
				buff = content.getBytes(encode); //将String转成指定charset的字节内容
			}
			
			out.write(buff, 0, buff.length);
			//关闭输出流
			out.close();
		} catch (IOException e) {
			System.out.println("saveDocContentToFile() out.write exception");
			e.printStackTrace();
			return false;
		}		
		return true;
		
	}
	
	public static boolean saveDataToFile(byte[] buff, String path, String name)
	{	
		if(buff == null)
		{
			System.out.println("saveDataToFile() buff is null");
			return false;
		}
		
		File folder = new File(path);
		if(!folder.exists())
		{
			if(folder.mkdirs() == false)
			{
				System.out.println("saveDataToFile() mkdir path:" + path + " Failed!");
				return false;
			}
		}
		
		boolean ret = false;
		String filePath = path + name;
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			out.write(buff, 0, buff.length);
			ret = true;
			
			//关闭输出流
			out.close();
			out = null;
		} catch (Exception e) {
			System.out.println("saveDataToFile() new FileOutputStream failed");
			e.printStackTrace();
		} finally {
			if(out != null)
			{
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	
	public static String readDocContentFromFile(String path, String name) 
	{	
		String filePath = path + name;
		String encode = getCharset(filePath);
		return  readDocContentFromFile(path, name, encode);
	}
	
	public static String readDocContentFromFile(String path, String name, String encode) 
	{	
		String filePath = path + name;
		try 
		{			
			File file = new File(filePath);
			if(!file.exists() || !file.isFile())
			{
				//System.out.println("readDocContentFromFile " +filePath+ " 不存在或不是文件");
				return null;
			}
			
			int fileSize = (int) file.length();
			//System.out.println("fileSize:[" + fileSize + "]");
			if(fileSize  <= 0)
			{
				return null;
			}
			
			byte buffer[] = new byte[fileSize];
			FileInputStream in;
			in = new FileInputStream(filePath);
			in.read(buffer, 0, fileSize);
			in.close();	

			String content = null;
			if(encode == null)
			{
				content = new String(buffer);
			}
			else
			{
				content = new String(buffer, encode);
			}
			//System.out.println("content:[" + content + "]");
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String readDocContentFromFile(String path, String name, int offset, int size) 
	{	
		String filePath = path + name;
		String encode = getCharset(filePath);
		return  readDocContentFromFile(path, name, encode, offset, size);
	}
	
	
	public static String readDocContentFromFile(String path, String name, String encode, int offset, int size) 
	{	
		
		System.out.println("readDocContentFromFile() encode:" + encode);
		String filePath = path + name;
		try 
		{			
			File file = new File(filePath);
			if(!file.exists() || !file.isFile())
			{
				//System.out.println("readDocContentFromFile " +filePath+ " 不存在或不是文件");
				return null;
			}
			
			int fileSize = (int) file.length();
			//System.out.println("fileSize:[" + fileSize + "]");
			if(fileSize  <= 0)
			{
				return null;
			}
			
			int readSize = fileSize > (offset + size) ? size: (fileSize - offset);
					
			byte buffer[] = new byte[readSize];
			FileInputStream in;
			in = new FileInputStream(filePath);
			in.read(buffer, offset, readSize);
			in.close();	

			String content = null;
			if(encode == null)
			{
				content = new String(buffer);
			}
			else
			{
				content = new String(buffer, encode);
			}
			//System.out.println("content:[" + content + "]");
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
    
    private static String getEncodeOfBuffer(byte[] buffer, int size) {
		// TODO Auto-generated method stub
		int encodeDetectBufLen = 0;
		byte [] encodeDetectBuf = null;

		if(size < 2)
		{
			return null;
		}
		
		if(size < 600)
		{
			encodeDetectBufLen = size;
			encodeDetectBuf = new byte[encodeDetectBufLen];
		}
		else
		{
			encodeDetectBufLen = 600;
			encodeDetectBuf = new byte[encodeDetectBufLen];
		}
		System.arraycopy(buffer, 0, encodeDetectBuf, 0, encodeDetectBufLen);
		String encode = getEncoding(encodeDetectBuf);
		System.out.println("getEncodeOfBuffer encode:[" + encode + "]");	

		return encode;
	}

	public static boolean copyFile(String srcFilePath,String dstFilePath,boolean cover){
        File srcFile=new File(srcFilePath);
        if(srcFile.exists() == false)
        {
    		System.err.println("copyFile() srcFilePath:" + srcFilePath + " not exists!");
    		return false;
        }

    	File dstFile=new File(dstFilePath);
    	if(cover == false && dstFile.exists())
    	{
        	//不允许覆盖
        	System.err.println("copyFile() " + dstFilePath + " exists!");
        	return false;
        }
        
    	boolean ret = false;
        FileInputStream in=null;
        FileOutputStream out=null;
        FileChannel inputChannel = null;    
        FileChannel outputChannel = null;   
    
        try {
	        //Copy by Channel
	        in=new FileInputStream(srcFilePath);
	        out=new FileOutputStream(dstFilePath);
	        inputChannel = in.getChannel();    
	        outputChannel = out.getChannel();   
	        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
	        inputChannel.close();
		    outputChannel.close();
		    in.close();
		    out.close();
		    ret = true;
        }
    	catch (Exception e) { 
    		System.err.println("copyFile() from " + srcFilePath + " to " + dstFilePath + " Exception"); 
    		e.printStackTrace(); 
    	} finally {
			try {
	    		if(inputChannel != null)
	    		{
	    			inputChannel.close();
	    		}
	    		if(outputChannel != null)
	    		{
					outputChannel.close();
	    		}
	    		if(in != null)
	    		{
	    			in.close();
	    		}
	    		if(out != null)
	    		{
	    			out.close();
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}

    	}
    	return ret;
    }
    
    //strict: true there is not file and dir, false: there is no file
	public static boolean isEmptyDir(String dirPath, boolean strict) 
	{
		//System.out.println("isEmptyDir() dirPath:" + dirPath);
		File dir = new File(dirPath);
		if(isEmptyDir(dir, strict) == true)
		{
			//System.out.println("isEmptyDir() " + dirPath + " 本地是空目录");
			return true;
		}
		return false;
	}
	
    //strict: true there is not file and dir, false: there is no file
	public static boolean isEmptyDir(File dir, boolean strict) 
	{
    	if(!dir.exists())
    	{
    		return true;
    	}
    	    	
    	File[] fileList = dir.listFiles();
    	if(fileList != null && fileList.length > 0)
    	{
    		if(strict)
    		{
    			return false;
    		}
    		
    		for(int i=0; i< fileList.length; i++)
    		{
    			if(fileList[i].isFile())
    			{
    				return false;
    			}
    			
    			if(isEmptyDir(fileList[i], strict) == false)
    			{
    				return false;
    			}
    		}
    	}    	
		return true;
	}

    public static boolean copyDir(String srcPath, String dstPath, boolean cover) 
    {
	    try {
	    	//Check the srcDir
	    	File srcDir = new File(srcPath); 
	    	if(srcDir.exists() == false)
	    	{
    			System.err.println("copyDir() srcPath not exists:"+srcPath);
    			return false;	    				    		
	    	}
	    	
	    	//Check the newPath
	    	File dstDir = new File(dstPath);
	    	if(dstDir.exists())
	    	{
	    		if(cover == false)
	    		{
	    			System.err.println("copyDir() dstPath exists:"+dstPath);
	    			return false;	    			
	    		}
	    	}
	    	else
	    	{
	    		//mkdirs will create the no exists parent dir, so I use the mkdir
	    		if(dstDir.mkdir() == false)
	    		{
	    			System.err.println("copyDir() Failed to create dir:"+dstPath);
	    			return false;
	    		}
	    	}
	    	
		    String[] file=srcDir.list(); 
		    File temp=null; 
		    for (int i = 0; i < file.length; i++) 
		    { 
		    	String subSrcFilePath = null;
		    	String subDstFilePath = null;
		    	if(srcPath.endsWith(File.separator))
		    	{ 
		    		subSrcFilePath = srcPath+file[i];
		    		subDstFilePath = dstPath + file[i];
		    	} 
		    	else
		    	{ 
		    		subSrcFilePath = srcPath+File.separator+file[i];
		    		subDstFilePath = dstPath+File.separator+file[i];
		    	} 

	    		temp=new File(subSrcFilePath); 
		    	if(temp.isFile())
		    	{ 
		    		copyFile(subSrcFilePath, subDstFilePath, cover);
		    	}
		    	else //if(temp.isDirectory()) //如果是子文件夹
		    	{ 
		    		copyDir(subSrcFilePath, subDstFilePath, cover); 
		    	} 
		    } 
	    } 
	    catch (Exception e) 
	    { 
	    	System.err.println("copyDir from " + srcPath  + " to " + dstPath + " 异常"); 
	    	e.printStackTrace(); 
	    	return false;
	    }
	    return true;
    }
    
    //Copy FileOrDir
    public static boolean copyFileOrDir(String srcPath, String dstPath,boolean cover){
	    //Check the newPath
	    File dstDir = new File(dstPath);
	    if(dstDir.exists())
	    {
	    	if(cover == false)
	    	{
	    		System.out.println("copyFileOrDir() dstPath exists:"+dstPath);
	    		return false;	    			
	    	}
	    }
	    
	    File srcDir = new File(srcPath);
	    if(srcDir.isFile())
	    {
	    	if(false == copyFile(srcPath, dstPath, cover))
	    	{
	    		System.out.println("copyFileOrDir() copyFile Failed:"+dstPath);
		    	return false;
	    	}
	    }
	    else
	    {
	    	if(false == copyDir(srcPath, dstPath, cover))
	    	{
	    		System.out.println("copyFileOrDir() copyDir Failed:"+dstPath);
		    	return false;
	    	}
	    }
	    return true;
	}
    
	public static boolean checkAddLocalDirectory(String localParentPath) {
		File parentDir = new File(localParentPath);
		if(parentDir.exists() == false)
		{
			return parentDir.mkdirs();
		}
		return true;		
	}
	
    //Create Directory
    public static boolean createDir(String path){
        File dir=new File(path);
        if(!dir.exists())
        {
            return dir.mkdirs();
        }
        else
        {
        	return true;
        }
    }
    
    //Create File
    public static boolean createFile(String path,String filename){
        File dir = new File(path);
        if(!dir.exists())
        {
        	if(dir.mkdirs() == false)
        	{
        		return false;
        	}
        }
        
    	File file=new File(path+"/"+filename);
        if(!file.exists())
        {    
        	try {
				return file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
        }
        else
        {
        	return true;
        }
    }
    
    //Delete File
    public static boolean delFile(String path){
        File file=new File(path);
        if(file.exists())
        {
        	if(file.isFile())
        	{
        		return file.delete();	
        	}
        	else
        	{
        		return false;
        	}
        }
        return true;
    }
    
    protected long getFileOrDirSize(File file, boolean isFile) {
		if(isFile)
		{
			return file.length();
		}
		
		return getFolderSize(file);
	}

    protected long getFolderSize(File file) {
		return FileUtils.sizeOfDirectory(file);
	}
    

    
    //Delete Directory, path must be dir path
    public static boolean delDir(String path){
        File dir=new File(path);
        if(dir.exists())
        {
            File[] tmp=dir.listFiles();            
            for(int i=0;i<tmp.length;i++)
            {
            	String subDirPath = path+"/"+tmp[i].getName();
                if(tmp[i].isDirectory())
                {
                    if(delDir(subDirPath) == false)
                    {
                    	System.out.println("delDir() delete subDir Failed:" + subDirPath);
                    	return false;
                    }
                }
                else
                {
                    if(tmp[i].delete() == false)
                    {
                    	System.out.println("delDir() delete subFile Failed:" + subDirPath);
                    	return false;
                    }
                }
            }
            if(dir.delete() == false)
            {
            	System.out.println("delDir() delete Dir Failed:" + path);
                return false;
            }
        }
        return true;
    }
	
    //Delete Directory or File
    public static boolean delFileOrDir(String path){
        File file=new File(path);
        if(file.exists())
        {
            if(file.isDirectory())
            {
	            File[] tmp=file.listFiles();            
	            for(int i=0;i<tmp.length;i++)
	            {
	            	String subDirPath = path+"/"+tmp[i].getName();
	                if(delFileOrDir(subDirPath) == false)
	                {
	                	System.out.println("delFileOrDir() delete subDir Failed:" + subDirPath);
	                    return false;
	                }
	            }
            }
            
            if(file.delete() == false)
            {
            	System.out.println("delFileOrDir() delete Dir Failed:" + path);
                return false;
            }
        }
        return true;
    }
    
    //Clear Directory
    public static boolean clearDir(String path){
        File file=new File(path);
        File[] tmp=file.listFiles();            
        for(int i=0;i<tmp.length;i++)
        {
        	String subDirPath = path+"/"+tmp[i].getName();
            if(delFileOrDir(subDirPath) == false)
            {
            	System.out.println("delFileOrDir() delete subDir Failed:" + subDirPath);
                return false;
            }
        }
        return true;
    }
    
    //检查文件是否存在
    public static boolean isFileExist(String path){
    	File file=new File(path);
        return file.exists();
    }
    
	public static String saveFile(MultipartFile srcFile,String path,String fileName)throws Exception{		
		if(fileName==null || "".equals(fileName))
		{
			System.out.println("saveFile() fileName is empty!");
			return null;
		}
		
		//底层接口不能主动创建上层目录，不存在上层目录则直接报错
		File forder1 = new File(path);
		if(!forder1.exists())
		{
			System.out.println("saveFile() path:" + path + " not exists!");
			forder1.mkdirs(); //创建目录
		}
		
		File dstFile = new File(path,fileName);
		
		srcFile.transferTo(dstFile);
		return fileName;
	}
	
    //Move FileOrDir
    public static boolean moveFileOrDir(String oldpath,String oldName,String newpath,String newName,boolean cover){
    	
    	String oldFilePath = oldpath + oldName;
    	String newFilePath = newpath + newName;
    	
    	if(!oldFilePath.equals(newFilePath))
        {
            File oldfile=new File(oldFilePath);
            if(oldfile.exists() == false)
            {
            	System.out.println("moveFile() oldFilePath:" + oldFilePath + " does not exist");
            	return false;
            }
            
            File newfile=new File(newFilePath);
            if(newfile.exists()) //若在待转移目录下，已经存在待转移文件
            {
            	System.out.println("moveFile() newFilePath:" + newFilePath + " already exists");
            	if(cover)//覆盖
                {
                	System.out.println("moveFile() 强制覆盖！");
                    return oldfile.renameTo(newfile);
                }
                else
                {
                    return false;
                }
            }
            else
            {
            	return oldfile.renameTo(newfile);
            }
        }
        else
        {
        	System.out.println("moveFile() newFilePath:" + newFilePath + " is same to oldFilePath:" + oldFilePath);
        	return true;
        }
    }
	
	//向文件末尾追加内容
    public static void appendContentToFile(String filePath, String content) {
        try {
            // 打开一个随机访问文件流，按读写方式
            RandomAccessFile randomFile = new RandomAccessFile(filePath, "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            //将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.writeBytes(content);
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getFileSuffix(String filePath)
    {
    	String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);
    	//System.out.println("getFileSuffix() " + suffix);
    	return suffix.toLowerCase();
    }
    
    public static String getOrgFileSuffix(String filePath)
    {
    	String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);
    	return suffix;
    }
    
	public static boolean isOfficeFile(String name) 
	{
		String fileSuffix = getFileSuffix(name);
		return isOffice(fileSuffix);
	}
	
	public static boolean isTextFile(String name) {
		String fileSuffix = getFileSuffix(name);
		return isText(fileSuffix);
	}
	
	protected boolean isTxzFile(String name) {
		int pos = name.toLowerCase().lastIndexOf(".txz") + 4;
		if(pos == name.length())
		{
			return true;
		}
		
		pos = name.toLowerCase().lastIndexOf(".tar.xz") + 7;
		return pos == name.length();
	}

	protected boolean isTgzFile(String name) {
		int pos = name.toLowerCase().lastIndexOf(".tgz") + 4;
		if(pos == name.length())
		{
			return true;
		}	
		
		pos = name.toLowerCase().lastIndexOf(".tar.gz") + 7;
		return pos == name.length();
	}

	protected boolean isTarBz2File(String name) {
		int pos = name.toLowerCase().lastIndexOf(".tbz2") + 5;
		if(pos == name.length())
		{
			return true;
		}
		
		pos = name.toLowerCase().lastIndexOf(".tar.bz2") + 8;
		return pos == name.length();
	}

	protected boolean isTarFile(String name) {
		int pos = name.toLowerCase().lastIndexOf(".tar") + 4;
		return pos == name.length();
	}
	
	protected boolean isBz2File(String name) {
		int pos = name.toLowerCase().lastIndexOf(".bz2") + 4;
		return pos == name.length();
	}
	
	protected boolean isXzFile(String name) {
		int pos = name.toLowerCase().lastIndexOf(".xz") + 3;
		return pos == name.length();
	}
	
	protected boolean isGzFile(String name) {
		int pos = name.toLowerCase().lastIndexOf(".gz") + 3;
		return pos == name.length();
	}

	protected boolean is7zFile(String name) {
		int pos = name.toLowerCase().lastIndexOf(".7z") + 3;
		return pos == name.length();
	}

	protected boolean isRarFile(String name) {
		int pos = name.toLowerCase().lastIndexOf(".rar") + 4;
		return pos == name.length();
	}
	
	protected boolean isZipFile(String name) {
		int pos = name.toLowerCase().lastIndexOf(".zip") + 4;
		return pos == name.length();
	}
	
	protected boolean isWarFile(String name) {
		int pos = name.toLowerCase().lastIndexOf(".war") + 4;
		return pos == name.length();
	}
	
	public static boolean isCompressFile(String name) {
		String fileSuffix = getFileSuffix(name);
		return isZip(fileSuffix);
	}
	
	public static String getCompressFileType(String name)
	{
		String fileSuffix = getFileSuffix(name);
		if(fileSuffix == null)
		{
			//"未知文件类型"
			return null;
		}
		
		switch(fileSuffix)
		{
		case "zip":
		case "war":
		case "rar":
		case "7z":
		case "tar":
		case "tgz":
		case "txz":
		case "tbz2":
			return fileSuffix;
		case "gz":
			int pos = name.toLowerCase().lastIndexOf(".tgz") + 4;
			if(pos == name.length())
			{
				return "tgz";
			}	
			pos = name.toLowerCase().lastIndexOf(".tar.gz") + 7;
			if(pos == name.length())
			{
				return "tar.gz";
			}
			return fileSuffix;
		case "xz":
			int pos1 = name.toLowerCase().lastIndexOf(".txz") + 4;
			if(pos1 == name.length())
			{
				return "txz";
			}	
			pos1 = name.toLowerCase().lastIndexOf(".tar.xz") + 7;
			if(pos1 == name.length())
			{
				return "tar.xz";
			}
			return fileSuffix;
		case "bz2":
			int pos2 = name.toLowerCase().lastIndexOf(".tbz2") + 5;
			if(pos2 == name.length())
			{
				return "tgz";
			}	
			pos2 = name.toLowerCase().lastIndexOf(".tar.bz2") + 8;
			if(pos2 == name.length())
			{
				return "tar.bz2";
			}
			return fileSuffix;
		default:
			return null;
		}
	}
	
	protected static boolean isZip(String fileSuffix) {
		if(fileSuffix == null)
		{
			//"未知文件类型"
			return false;
		}

		switch(fileSuffix)
		{
		case "zip":
		case "war":
		case "rar":
		case "7z":
		case "tar":
		case "tgz":
		case "gz":
		case "txz":
		case "xz":
		case "bz2":
		case "tbz2":
			return true;
		default:
			break;
		}
		return false;
	}
	
	public static boolean isPdf(String fileSuffix) {
		if(fileSuffix == null)
		{
			//"未知文件类型"
			return false;
		}

		switch(fileSuffix)
		{
		case "pdf":
			return true;
		default:
			break;
		}
		return false;
	}

	public static boolean isText(String fileSuffix) {
		if(fileSuffix == null)
		{
			//"未知文件类型"
			return false;
		}

		switch(fileSuffix)
		{
		case "txt":
		case "log":	
		case "md":	
		case "py":
		case "java":
		case "cpp":
		case "hpp":
		case "c":
		case "h":
		case "json":
		case "xml":
		case "html":
		case "sql":
		case "js":
		case "css":
		case "jsp":
		case "php":
		case "properties":
		case "conf":
		case "out":
		case "sh":
		case "bat":
		case "msg":
		case "cmake":
			return true;
		default:
			break;
		}
		return false;
	}
	public static boolean isPicture(String fileSuffix) {
		if(fileSuffix == null)
		{
			//"未知文件类型"
			return false;
		}
		
		switch(fileSuffix)
		{
		case "jpg":
		case "jpeg":
		case "png":
		case "gif":
		case "bmp":
		case "mpg":
			return true;
		default:
			break;
		}
		return false;
	}
	
	protected boolean isVideo(String fileSuffix) {
		if(fileSuffix == null)
		{
			//"未知文件类型"
			return false;
		}
		
		switch(fileSuffix)
		{
		case "avi":
		case "mov":
		case "mpeg":
		case "mpg":
		case "mp4":
		case "rmvb":
		case "asf":
		case "flv":
		case "ogg":
			return true;
		default:
			break;
		}
		return false;
	}
	
	public static boolean isOffice(String fileSuffix) {
		if(fileSuffix == null)
		{
			//"未知文件类型"
			return false;
		}
		
		switch(fileSuffix)
		{
		case "doc":
		case "docx":
		case "xls":
		case "xlsx":
		case "ppt":
		case "pptx":
			return true;
		default:
			break;
		}
		return false;
	}	
	
    public static boolean compressExe(String srcPathName,String finalFile) {
    	File zipFile = new File(finalFile);	//finalFile
    	
        File srcdir = new File(srcPathName); //srcFile or Dir
        if (!srcdir.exists()){
        	System.out.println(srcPathName + "不存在！");
        	return false;
        }   
            
        Project prj = new Project();    
        Zip zip = new Zip();    
        zip.setProject(prj);    
        zip.setDestFile(zipFile);    
        FileSet fileSet = new FileSet();    
        fileSet.setProject(prj);    
        fileSet.setDir(srcdir);    
        //fileSet.setIncludes("**/*.java"); //包括哪些文件或文件夹 eg:zip.setIncludes("*.java");    
        //fileSet.setExcludes(...); //排除哪些文件或文件夹    
        zip.addFileset(fileSet);    
        zip.execute();  
		
        if(zipFile.exists())
        {
        	return true;
        }
        return false;
    }
    
    
	/***************************图片上传相关接口*****************************/
	protected static String[] IMGALLOWDTYPES = {"JPG","JPEG","PNG","GIF","BMP"};
	
	/**
	 * 保存前台传回的图片
	 * @param imgFile
	 * @param path 保存地址
	 * @oaram compressPath 压缩图片地址
	 * @return 上传文件名称
	 */
	public String saveImg(MultipartFile imgFile,String path,String compressPath,boolean limitType)throws Exception{
		String imgName = imgFile.getOriginalFilename();
		String ext = imgName.substring(imgName.lastIndexOf('.')+1);
		ext = ext.toLowerCase();
		//可以上传的图片类型
		//定义一个数组，用于保存可上传的文件类型
		
		
		long fileSize = imgFile.getSize();
		if(fileSize==0){
			return null;
		}
		System.out.println("文件大小：" + Math.floor(fileSize/1024));
		if(limitType){
			if(fileSize>200*1024*1024){
				throw new FileUploadException("上传文件过大");
			}
			List<String> fileTypes = new ArrayList<String>();
			fileTypes.add("jpg");
			fileTypes.add("jpeg");
			fileTypes.add("bmp");
			fileTypes.add("gif");
			fileTypes.add("png");
			if(!fileTypes.contains(ext)){
				throw new Exception("上传文件格式不支持");
			}
		}else{
			if(fileSize>20*1024*1024){
				throw new FileUploadException("上传图片过大");
			}
		}
		File _imgFile = null;
		String _fileName = "";
		
		if(imgName!=null&&!"".equals(imgName)){
			File forder1 = new File(path);
			if(forder1.exists()){
			}else{
				forder1.mkdirs();
			}
			_fileName = generateDateAndRadom() + "." + ext;
			_imgFile = new File(path,_fileName);
			try {
				imgFile.transferTo(_imgFile);
				//压缩图片到smallPic目录
				if(limitType&&path!=null&&!path.equals("")&&compressPath!=null&&!compressPath.equals("")){
					CompressPic cp = new CompressPic();
					cp.setInputDir(path);
					cp.setOutputDir(compressPath);
					cp.setInputFileName(_imgFile.getName());
					cp.setOutputFileName(_imgFile.getName());
					cp.compressPic();
				}
			} catch (Exception e) {
				throw new Exception("上传图片保存本地图片失败，源文件名：" + imgName);
			}
		}
		return _fileName;
	}
	
	public ReturnAjax saveImgAjax(String folder,String file,String fileName,String compressPath,boolean limitType)throws Exception{
		ReturnAjax rt = new ReturnAjax();
		String ext = fileName.substring(fileName.lastIndexOf("."));
		long fileSize = file.length();
		System.out.println("文件大小：" + Math.floor(fileSize/1024));
		if(fileSize==0){
			return null;
		}
		if(limitType){
			if(fileSize>20*1024*1024){
				throw new FileUploadException("上传文件过大");
			}
			List<String> fileTypes = new ArrayList<String>();
			fileTypes.add("jpg");
			fileTypes.add("jpeg");
			fileTypes.add("bmp");
			fileTypes.add("gif");
			fileTypes.add("png");
			if(!fileTypes.contains(ext)){
				throw new Exception("上传文件格式不支持");
			}
		}else{
			if(fileSize>200*1024*1024){
				throw new FileUploadException("上传图片过大");
			}
		}
		try {
			String _fileName = generateDateAndRadom() + ext;
			Base64File.decode(file, folder + File.separator, _fileName);
			System.out.println("上传路径："+folder+";上传名称：" + _fileName);
			//压缩图片到smallPic目录
			if(limitType&&folder!=null&&!folder.equals("")&&compressPath!=null&&!compressPath.equals("")){
				CompressPic cp = new CompressPic();
				cp.setInputDir(folder);
				cp.setOutputDir(compressPath);
				cp.setInputFileName(_fileName);
				cp.setOutputFileName(_fileName);
				cp.compressPic();
			}
			rt.setData(_fileName);
		} catch (Exception e) {
			e.printStackTrace();
			rt.setError("上传图片失败。");
			rt.setData("上传图片失败。");
		}
		return rt;
	}
	
	protected String generateDateAndRadom(){
		Date date = new Date();
		String dateStr = DateFormat.dateTimeFormat2(date);
		String r = Math.round(Math.random()*100000)+"";
		System.out.println(dateStr+";"+r);
		return "freeteam"+dateStr+"_"+r;
	}
	
	protected boolean checkImgType(String type) {
		String upperType = type.toUpperCase();
		for(String s: IMGALLOWDTYPES){
			if(upperType.equals(s)||upperType.endsWith(s)){
				return true;
			}
		}
		return false;
	}
    
	public static boolean checkEncoding(byte[] bytes, String encode) 
	{   
		String str;
		try {
			str = new String(bytes, encode);
			System.out.println("checkEncoding() str:" + str);
	        if(Arrays.equals(str.getBytes(), bytes)) 
			{   
	        	return true;    
	        }
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return false;
	}

	static String getEncoding(byte[] bytes)
	{
		String [] encodeCheckList = {
				 "UTF-8",
				 "GBK",
				 "GB2312",
				 "ISO-8859-1",
				 "ASCII",
				 "UTF-16",
				 "GB18030",
		};
		for(int i=0; i<encodeCheckList.length; i++)
		{
			if(checkEncoding(bytes, encodeCheckList[i]) == true)
			{
				return encodeCheckList[i];
			}
		}
		return null;
    }
	
	//This interface was supplied by 寞寞柒柒
	public static String getCharset(String path) {
		File file = new File(path);
		if(file.exists() == false)
		{
			System.out.println("getCharset() file not exist");
			return null;
		}
		
		String charset = "GBK";
		byte[] first3Bytes = new byte[3];
		try {
			boolean checked = false;
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
			bis.mark(0); // 注： bis.mark(0);修改为 bis.mark(100);我用过这段代码，需要修改上面标出的地方。
			// 注：不过暂时使用正常，遂不改之
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1) {
				bis.close();
				return charset; // 文件编码为 ANSI
			} else if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
				charset = "UTF-16LE"; // 文件编码为 Unicode
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
				charset = "UTF-16BE"; // 文件编码为 Unicode big endian
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
				charset = "UTF-8"; // 文件编码为 UTF-8
				checked = true;
			}
			bis.reset();
			if (!checked) {
				while ((read = bis.read()) != -1) {
					if (read >= 0xF0)
						break;
					if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
						break;
					if (0xC0 <= read && read <= 0xDF) {
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
							// (0x80 - 0xBF),也可能在GB编码内
							continue;
						else
							break;
					} else if (0xE0 <= read && read <= 0xEF) { // 也有可能出错，但是几率较小
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) {
							read = bis.read();
							if (0x80 <= read && read <= 0xBF) {
								charset = "UTF-8";
								break;
							} else
								break;
						} else
							break;
					}
				}
			}
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
			charset = null;
		}
		System.out.println("charset:" + charset);
		return charset;
	}
	
	/**
	 * 获取文件编码格式
	 * @param filePath
	 * @return UTF-8/Unicode/UTF-16BE/GBK
	 * @throws Exception
	 */
	public static String getFileEncode(String filePath)
	{
        String charsetName = null;
        try {
            File file = new File(filePath);
            CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
            detector.add(new ParsingDetector(false));
            detector.add(JChardetFacade.getInstance());
            detector.add(ASCIIDetector.getInstance());
            detector.add(UnicodeDetector.getInstance());
            java.nio.charset.Charset charset = null;
            charset = detector.detectCodepage(file.toURI().toURL());
            if (charset != null) {
                charsetName = charset.name();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return charsetName;
	}
}
