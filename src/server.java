
import com.sun.net.httpserver.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Map;
import java.util.stream.Collectors;

public class server {
    public static void main(String[] args) throws IOException {
        // 建立一個綁定在 localhost:8000 的 server
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        // 綁定 API 路由
        server.createContext("/api/attendance", new TableDataHandler("select * from attendance"));
        server.createContext("/api/books", new TableDataHandler("select * from books"));
        server.createContext("/api/booktype", new TableDataHandler("select * from booktype"));
        server.createContext("/api/empoloyees", new TableDataHandler("select * from empoloyees"));
        server.createContext("/api/members", new TableDataHandler("select * from members"));
        
        server.createContext("/api/insertdata", new InsertDataHandler());


        server.createContext("/chat/send", new ChatHandler.SendHandler());
        server.createContext("/chat/messages", new ChatHandler.MessageHandler());
        server.createContext("/postdemo",new postdemo());
        server.createContext("/practice",new practice());
        
        // 啟動 server
        server.start();
        System.out.println("Server started at http://localhost:8000");
    }
    static class postdemo implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String file = "temppost.txt";
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(body);
            }
        }
    }    
    static class practice implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "！禮拜六早上好睏！";
        
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            byte[] responseBytes = response.toString().getBytes("UTF-8");
            exchange.sendResponseHeaders(200,responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }
     // 定義 handler
   static class TableDataHandler implements HttpHandler {
        String sql="";
        public TableDataHandler(String sql){
            this.sql=sql;
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "";
            StringBuilder json = new StringBuilder();
            json.append("[");
            try {
                ResultSet rs = DBConnect.selectQuery(sql);
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
            
                boolean firstRow = true;
                while (rs.next()) {
                    if (!firstRow) json.append(",");
                    firstRow = false;
            
                    json.append("{");
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = meta.getColumnLabel(i);
                        String value = rs.getString(i);
                        if (i > 1) json.append(",");
                        json.append("\"").append(columnName).append("\":");
                        json.append("\"").append(value == null ? "" : escapeJson(value)).append("\"");
                    }
                    json.append("}");
                }
                rs.getStatement().getConnection().close(); // 關閉連線
            }
        catch (Exception e) {
                json = new StringBuilder("{\"error\":\"" + e.getMessage() + "\"}");
            }
            
            json.append("]");
        
            response = json.toString();
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            byte[] responseBytes = response.toString().getBytes("UTF-8");
            exchange.sendResponseHeaders(200,responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
        private static String escapeJson(String s) {
            return s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
        }
        
    }
//還需要修改 (多資料回傳json)
    static class InsertDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
                // ✅ 處理 CORS 預檢請求（OPTIONS）
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1); // 204 No Content
            return;
        }
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");    try{
            String body = new BufferedReader( new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining());
            Map<String,String> map = DBConnect.parseJson(body);
            String targetTable = map.get("table"); 
            switch(targetTable){
                case "members":
                    String membername = map.get("membername");
                    String mobile = map.get("mobile");
                    String deposit = map.get("deposit");
                    int memberid = DBConnect.getNextId("members", "memberid");
                    int result = DBConnect.executeUpdate("INSERT INTO members (memberid, membername,mobile,deposit) VALUES (?, ?,?, ?)", memberid,membername,mobile,deposit);

                    exchange.sendResponseHeaders(200, -1);
                    break;
                case "books":
                    String bookname = map.get("bookname");
                    String btype = map.get("btype");
                    String price = map.get("price");
                    String stock = map.get("stock");
                    String productdate = map.get("productdate");
                    String areasection = map.get("areasection");
                    int bookid = DBConnect.getNextId("books", "bookid");
                    int result2 = DBConnect.executeUpdate("INSERT INTO books (bookid, bookname,btype,price,stock,productdate,areasection) VALUES (?, ?,?,?,?,?,?)", bookid,bookname,btype,price,stock,productdate,areasection);
                    exchange.sendResponseHeaders(200, -1);
                    break;
                case "renttickets":
                    String tmemberid = map.get("memberid");
                    String tbookid = map.get("bookid");
                    String rentdate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String empoloyee = map.get("empoloyee");
                    int backday = 3;
                    String event = map.get("event");
                    int rentid = DBConnect.getNextId("renttickets", "rentid");
                    int result3 = DBConnect.executeUpdate("INSERT INTO renttickets (rentid, memberid, bookid, rentdate, empoloyee, backday, event) VALUES (?, ?, ?, ?, ?, ?, ?)",rentid, tmemberid, tbookid, rentdate, empoloyee, backday, event);
                    exchange.sendResponseHeaders(200, -1);
                    break;
                default:
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
            String response = "{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(500, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
        }
    }


}


