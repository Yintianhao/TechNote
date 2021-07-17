/*
 * @Author: Yintianhao
 * @Date: 2020-05-08 15:55:00
 * @LastEditTime: 2020-05-08 16:48:08
 * @LastEditors: Yintianhao
 * @Description: 
 * @FilePath: \TechNote\src\Code\DailyPractice\ReadWriteFileTest.java
 * @Copyright@Yintianhao
 */
package DailyPractice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ReadWriteFileTest{
    public static void main(String[] args){
        String projectDir = System.getProperty("user.dir");
        String filePath = projectDir+"/src/Code/DailyPractice/file.txt";
        try{
            writeFile(filePath, "me,too");
            readFile(filePath);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void readFile(String path) throws IOException{
        File file = new File(path);
        //创建一个File对象
        //然后创建一个文件输入流，将上面的File对象作为参数传入。
        //然后创建一个输入流读类，就是InputStreamReader，然后将文件输入流对象作为参数传入
        //然后用while循环读取，读取之后用Readline方法进行判断，如果读到的是空对象，就读取完毕
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = "";
        line = bufferedReader.readLine();
        while(line != null){
            System.out.println(line);
            line = bufferedReader.readLine();
        }
    }
    public static void writeFile(String path,String content){
        try{
            //新建一个File对象
            //新建一个文件输出流对象，将File类型进行传入
            //新建一个BufferedWriter对象，将之前的输出流对象作为参数传入
            //利用对象的append()方法进行写入
            File file = new File(path);
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file,true));
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.append(content);
            bufferedWriter.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}