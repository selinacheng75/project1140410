
import java.sql.*;
import java.util.Date;
import java.util.Scanner;


public class ShopMenu {
   private static Scanner scanner = new Scanner(System.in);
   public static void main(String[] args) {
       while (true) {
           showMenu();
           int choice = getChoice();
           handleChoice(choice);
       }
   }


   private static void showMenu() {
       System.out.println("\n====== Shop System ======");
       System.out.println("1. 新增會員");
       System.out.println("2. 查詢會員");
       System.out.println("3. 新增書籍");
       System.out.println("4. 查詢書籍");
       System.out.println("5. 新增租借記錄");
       System.out.println("6. 歸還租借記錄");
       System.out.println("7. 過期未還記錄");
       System.out.println("8. 各類型書籍營收狀況");
       System.out.println("9. 熱門書本排行");
       System.out.println("10. VIP排行");
       System.out.println("11. 員工薪資總表");
       System.out.println("12. 離開");
       System.out.print("請選擇功能（輸入數字）：");
   }




   private static int getChoice() {
       try {
           return Integer.parseInt(scanner.nextLine());
       } catch (NumberFormatException e) {
           System.out.println("⚠️ 輸入錯誤，請輸入數字！");
           return -1;
       }
   }


   private static void handleChoice(int choice) {
       switch (choice) {
           case 1 -> addmembers();
           case 2 -> listmembers();
           case 3 -> addbooks();
           case 4 -> listbooks();
           case 5 -> addrent();
           case 6 -> returnrent();
           case 7 -> overdate();
           case 8 -> typerevenue();
           case 9 -> popularbook();
           case 10 -> topvip();
           case 11 -> salary();
           case 12 -> {
               System.out.println("👋 感謝使用，系統退出。");
               System.exit(0);
           }
           default -> System.out.println("⚠️ 無效選擇，請重新輸入！");
       }
   }




   private static void addmembers() {
       int id = getNextId("members", "memberid");
       System.out.print("請輸入客戶名稱：");
       String name = scanner.nextLine();
       System.out.print("請輸入客戶電話：");
       String tel = scanner.nextLine();
       System.out.print("請輸入客戶儲金：");
       String money = scanner.nextLine();
       int result = DBConnect.executeUpdate("INSERT INTO members (memberid, membername, mobile, deposit ) VALUES (?, ?, ?, ?)", id, name,tel,money);
       System.out.println(result > 0 ? "✅ 客戶新增成功！" : "❌ 客戶新增失敗！");
   }




   private static void listmembers() {
       try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM members")) {
            System.out.println("================================");
            System.out.println("ID | 客戶名稱");
           while (rs.next()) {
               System.out.println(rs.getInt("memberid") + " | " + rs.getString("membername")+ " | " + rs.getString("mobile")+ " | " + rs.getString("deposit"));
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
   private static void addbooks() {
       int bookid = getNextId("books", "bookid");
       System.out.print("請輸入書籍名稱：");
       String bookname = scanner.nextLine();
       System.out.print("請輸入書籍類型：(1.技術 2.市場 3.食物 4.流行 5.小說 6.其它)");
       String booktype = scanner.nextLine();
       System.out.print("請輸入書籍租價：");
       String rentprice = scanner.nextLine();
       System.out.print("請輸入庫存數量：");
       String stock = scanner.nextLine();
       System.out.print("請輸入生產日期：");
       String prdate = scanner.nextLine();
       System.out.print("請輸入書籍區域：");
       String area = scanner.nextLine();
       int result = DBConnect.executeUpdate("INSERT INTO books (bookid, bookname, btype, price,stock,productdate,areasection ) VALUES (?, ?, ?, ?, ?, ?, ?)",bookid, bookname,booktype,rentprice,stock,prdate,area);
       System.out.println(result > 0 ? "✅ 書本新增成功！" : "❌ 書本新增失敗！");
   }


   private static void listbooks() {
       try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM books")) {
           System.out.print("===================================================================");
           System.out.println("ID | 書籍名稱 | 書籍類型 | 書籍租價 | 書籍庫存 | 生產日期 | 置放區域");
           while (rs.next()) {
               System.out.println(rs.getInt("bookid") + " | " + rs.getString("bookname")+ " | " + rs.getString("btype")+ " | " + rs.getString("price")+ " | " + rs.getString("stock")+ " | " + rs.getString("productdate")+ " | " + rs.getString("areasection"));
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
   private static void addrent() {
       int rentid = getNextId("renttickets", "rentid");
       System.out.print("請輸入租借人代號：");
       String memberid = scanner.nextLine();
       System.out.print("請輸入租借書籍編號：");
       String bookid = scanner.nextLine();
       double bookprice = getBookPrice(bookid);  // 查詢書籍價格
       double depositleft = getdepositleft(memberid);
       int total = (int)(depositleft - bookprice);
       java.sql.Timestamp today = new java.sql.Timestamp(new Date().getTime());
       System.out.print("請輸入員工編號：");
       String empoloyee = scanner.nextLine();
       System.out.print("請輸入使用活動代碼(如無活動則輸入0)：");
       String event = scanner.nextLine();
       int result = DBConnect.executeUpdate("INSERT INTO renttickets (rentid, memberid, bookid, rentdate,empoloyee,`return` ,event ) VALUES (?, ?, ?, ?, ?, 0 ,?)",rentid, memberid,bookid,today,empoloyee,event);
       System.out.println(result > 0 ? "✅ 新增成功！共扣款："+ bookprice +"元" + "剩餘：" + total + "元" : "❌ 新增失敗！");
       int UpdateResult = DBConnect.executeUpdate("UPDATE members SET deposit = ? WHERE memberid = ?",total, memberid);

   }
   private static double getBookPrice(String bookid) {
       try (ResultSet rs = DBConnect.selectQuery("SELECT price FROM books WHERE bookid = ?", bookid)) {
           if (rs.next()) {
               return rs.getDouble("price");
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
       return -1;  // 如果找不到價格，返回 -1
   }
   private static double getdepositleft(String borrower) {
       try (ResultSet rs = DBConnect.selectQuery("SELECT deposit FROM members WHERE memberid = ?", borrower)) {
           if (rs.next()) {
               return rs.getDouble("deposit");
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
       return -1;  // 如果找不到價格，返回 -1
   }


   private static void returnrent() {
       System.out.print("請輸入租借記錄的代號：");
       String returnnew = scanner.nextLine();
       String query = "UPDATE renttickets SET `return` = 1 WHERE rentid = ?";
       int result = DBConnect.executeUpdate(query, returnnew);
   }


   private static void overdate() {
       try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM overdate")) {
           System.out.print("====================================================");
           System.out.println("!!逾期未歸還訂單如下：");
           while (rs.next()) {
               System.out.println(rs.getString("rentdate") + " | " + rs.getString("membername") + " | " + rs.getString("mobile") + " | "  + rs.getString("bookname"));
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
   private static void typerevenue() {
       try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM revenue")) {
           System.out.print("============================================");
           System.out.println("各類書籍營收如下：");
           while (rs.next()) {
               System.out.println(rs.getString("btype") + " | " + rs.getString("typename") + " | " + rs.getString("benefit"));
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
   private static void popularbook() {
       try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM popularbook")) {
           System.out.print("================================================================");
           System.out.println("目前熱門書本排行如下：");
           while (rs.next()) {
               System.out.println(rs.getString("bookid") + " | " + rs.getString("bookname") + " | "+ rs.getString("renttotal"));
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
   private static void topvip() {
       try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM Topvip")) {
           System.out.print("===========================================================");
           System.out.println("目前vip租借排行如下：");
           while (rs.next()) {
               System.out.println(rs.getString("memberid") + " | " + rs.getString("membername") + " | "+ rs.getString("totalspend") );
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
   private static void salary() {
       try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM salarylist")) {
           System.out.print("================================================================");
           System.out.println("員工薪資如下：");
           while (rs.next()) {
               System.out.println(rs.getString("eid") + " | " + rs.getString("name") + " | "+ rs.getString("absentday")+ " | "+ rs.getString("deduct")+ " | "+ rs.getString("Total"));
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
  
   private static int getNextId(String tableName, String columnName) {
       try (ResultSet rs = DBConnect.selectQuery("SELECT MAX(" + columnName + ") FROM " + tableName)) {
           if (rs.next()) {
               return rs.getInt(1) + 1;
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
       return 1;
   }
}
