package helloworld;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageGrabber {

        public static String loadWebPage(String urlString) {
            byte[] buffer = new byte[80*1024];
            String content = new String();
            try {
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                connection.connect();
                BufferedReader r = new BufferedReader(new
                        InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
                String line;
                while ((line = r.readLine()) != null) {
                    content += line;
                }
            } catch (IOException e) {
                content = e.getMessage() + ": " + urlString;
            }
            return content;
        }

        public static void main(String[] args) throws Exception {
            /*
            String firstUrlAddress = "https://www.comp.hkbu.edu.hk/~bchoi/hktvmall.html";
           // System.out.println(loadWebPage(firstUrlAddress));
            String result = loadWebPage(firstUrlAddress);
            System.out.println("</span><button");
            String [] price = result.split("\\s+");
            for (int i=0; i<price.length; i++){
                //System.out.println(price[i]);
                if(price[i].contains("</span><button")){
                    System.out.println(price[i]);
                    String [] value = price[i].split("[$<]");
                    for (int j=0; j< value.length; j++){
                        if(value[j].matches(".*\\d.*")){
                            System.out.println(value[j]);
                        }

                    }
                }
            }
*/
            String price01 = "8,980.00";
            String [] correctPrice = price01.split("\\,");
            price01 = correctPrice[0]+correctPrice[1];
            String price02 = "8,990";
            String [] correctSecPrice = price02.split("\\,");
            price02 = correctSecPrice[0]+correctSecPrice[1];
            String cheaperCompany = "";

            double firstPrice = Double.parseDouble(price01);
            double secPrice = Double.parseDouble(price02);


            if(firstPrice<secPrice){
                cheaperCompany = "Fortress is cheaper";
            }else if (firstPrice>secPrice){
                cheaperCompany = "HKTVMall is cheaper";
            }else if (firstPrice==secPrice){
                cheaperCompany = "They are the same price";
            }
            System.out.println(cheaperCompany);
        }

}
