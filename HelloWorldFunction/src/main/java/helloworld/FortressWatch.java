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
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FortressWatch implements RequestHandler<Object, Object> {

    //excluded the credentials.
    final String accessKey = "AKIATETUL4TDGWOYNGL6";
    final String secretKey = "QltknEYceqmqTwEsbnPrQdvKSjdstOrWPhznbFbP";
    final String bucket = "hkbu.17228522";
    final String region = "us-east-1";  /// change it if your selected region is different
    String price;
    @Override
    public Object handleRequest(Object o, Context context) {

        int status = 200;
        String firstUrlAddress = "https://www.comp.hkbu.edu.hk/~bchoi/fortress.html";
        String content = loadWebPage(firstUrlAddress);
        //get the price from fortress
        String [] price = content.split("\\s+");
        for (int i=0; i<price.length; i++){
            if(price[i].contains("<div><p>")){ // the pattern where show the price
                System.out.println(price[i]);
                String [] value = price[i].split("[$<]");//clean the gotten date to be price
                for (int j=0; j< value.length; j++){
                    if(value[j].matches(".*\\d.*")){
                        System.out.println(value[j]);
                        this.price = value[j];
                        break;
                    }
                }
            }
        }
        System.out.println(price);
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
