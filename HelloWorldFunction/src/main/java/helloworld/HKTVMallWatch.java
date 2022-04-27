package helloworld;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class HKTVMallWatch implements RequestHandler<Object, Object> {
    //excluded the credentials.
    final String accessKey = "";
    final String secretKey = "";
    final String bucket = "";
    final String region = "";  /// change it if your selected region is different
    String price;
    @Override
    public Object handleRequest(Object o, Context context) {

        int status = 200;
        String firstUrlAddress = "https://www.comp.hkbu.edu.hk/~bchoi/hktvmall.html";
        String content = loadWebPage(firstUrlAddress);
        //get the price from hktv mall
        String [] price = content.split("\\s+");
        for (int i=0; i<price.length; i++){
            //System.out.println(price[i]);
            if(price[i].contains("</span><button")){  // the price must be include in this format
                //System.out.println(price[i]);
                String [] value = price[i].split("[$<]");
                for (int j=0; j< value.length; j++){
                    if(value[j].matches(".*\\d.*")){ //clean the gotten string to be a price
                        System.out.println(value[j]);
                        this.price = value[j];
                        break;
                    }

                }
            }
        }
        //System.out.println(price);
        /// change the region and credentials to your own regions and credentials.
        String region = this.region;    /// replace this string with your selected region if you use another one
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        String bucket = this.bucket;
        String objKey = "FortressWatch";

        /// Serialize the hash map and write the data to S3
        AmazonS3 s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();

        s3Client.putObject(bucket, objKey, this.price);

        /// put the json string to the response object
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setHeaders(headers);
        response.setStatusCode(status);

        return response
                .withHeaders(headers)
                .withBody(this.price)
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


    private static String serialize(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
