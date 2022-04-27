package helloworld;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class PriceDiff implements RequestHandler<Object, Object> {
    final String accessKey = "";
    final String secretKey = "";
    final String bucket = "";
    final String region = "";  /// change it if your selected region is different
    String hktvmallprice;
    String fortressprice;
    String cheaperCompany;
    @Override
    public Object handleRequest(Object o, Context context) {
        int status = 200;
        String firstUrlAddress = "https://www.comp.hkbu.edu.hk/~bchoi/hktvmall.html";
        String hktvcontent = loadWebPage(firstUrlAddress);
        // get the price from hktv mall
        String [] price = hktvcontent .split("\\s+");
        for (int i=0; i<price.length; i++){
            if(price[i].contains("</span><button")){
                String [] value = price[i].split("[$<]");
                for (int j=0; j< value.length; j++){
                    if(value[j].matches(".*\\d.*")){
                        System.out.println(value[j]);
                        this.hktvmallprice = value[j];
                        break;
                    }

                }
            }
        }

        String SectUrlAddress = "https://www.comp.hkbu.edu.hk/~bchoi/fortress.html";
        String fortresscontent = loadWebPage(SectUrlAddress);
       // get the price from fortress
        String [] fortressprice = fortresscontent.split("\\s+");
        for (int i=0; i<fortressprice.length; i++){
            if(fortressprice[i].contains("<div><p>")){
                String [] value = fortressprice[i].split("[$<]");
                for (int j=0; j< value.length; j++){
                    if(value[j].matches(".*\\d.*")){
                        System.out.println(value[j]);
                        this.fortressprice = value[j];
                        break;
                    }
                }
            }
        }

        // to convert the prices from both place to the form which is able to be converted to integer
        String pricefortress = this.fortressprice;
        String [] correctPrice = pricefortress.split("\\,");
        pricefortress = correctPrice[0]+correctPrice[1];
        String pricehktvmall =  this.hktvmallprice;
        String [] correctSecPrice = pricehktvmall.split("\\,");
        pricehktvmall = correctSecPrice[0]+correctSecPrice[1];
        String cheaperCompany = "";

        double firstPrice = Double.parseDouble(pricefortress);
        double secPrice = Double.parseDouble(pricehktvmall);

        //compare the price
        if(firstPrice<secPrice){
            cheaperCompany = "Fortress is cheaper";
        }else if (firstPrice>secPrice){
            cheaperCompany = "HKTVMall is cheaper";
        }else if (firstPrice==secPrice){
            cheaperCompany = "They are the same price";
        }
        System.out.println(cheaperCompany);
        //System.out.println(price);
        /// change the region and credentials to your own regions and credentials.
        String region = this.region;    /// replace this string with your selected region if you use another one
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        String bucket = this.bucket;
        String objKey = "PriceDiff";

        /// Serialize the hash map and write the data to S3
        AmazonS3 s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();

        s3Client.putObject(bucket, objKey, cheaperCompany);

        /// put the json string to the response object
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setHeaders(headers);
        response.setStatusCode(status);

        return response
                .withHeaders(headers)
                .withBody(cheaperCompany)
                .withStatusCode(status);
    }

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
}
