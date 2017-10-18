package Bookstore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Scanner;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

/*
 * Class: CITC1311 Programming II 
 * Instructor: Gitti Negahban
 * Programmer: Jonathan Knight
 * Description: This program functions as a bookstore, complete with a shopping cart 
 *              and the ability to search for an individual book by ISBN. All of the
 *              results and information of each book is retrieved via the Google Books 
 *              API.
 */


public class ShoppingCart extends Application
{   
    //  Creates and initializes all neccessary private access variables and objects.
    private final NumberFormat fmt1 = NumberFormat.getCurrencyInstance();
    private final double TAX_RATE = 1.09;
    private double subtotal = 0;
    private int lineCount = 0;
    private ArrayList<Book> shoppingCart = new ArrayList<>();
    private ArrayList<Book> library = new ArrayList<>();
    private Label lblSubTotal = new Label(fmt1.format(0.00));
    private Label lblCart = new Label("Shopping Cart");
    private VBox cartList = new VBox();
    private VBox cart = new VBox();
    private VBox list = new VBox();
    private FlowPane cartLbl = new FlowPane();
    private FlowPane cartBtns = new FlowPane();
    private GridPane cartFooter = new GridPane();     
    private GridPane cartHeader = new GridPane();
    private ScrollPane scrollPane = new ScrollPane();
    private ScrollPane spCart = new ScrollPane();
    private BorderPane window = new BorderPane();            
    private TextField tfSearch = new TextField();
    private Button btSearch = new Button("GO");
    private Button btHome = new Button("Home");
    private Button checkout = new Button("Checkout");
    private Button remove = new Button("Remove Selected Items");
    private HBox searchBar = new HBox(new Label("Search by ISBN: "), tfSearch, btSearch, btHome);
    private MenuBar menuBar = new MenuBar();
    private Menu menuFile = new Menu("File");
    private Menu menuHelp = new Menu("Help");
    private Menu menuExit = new Menu("Exit");
    private Menu menuAbout = new Menu("About");
        
    //  Displays the main navigation window.
    @Override
    public void start(Stage stage) throws FileNotFoundException, IOException
    {    
        //  Adds the information for every ISBN in the library.txt file to the 
        //  library array list. 
        File file = new File("library.txt");
        try (Scanner inputFile = new Scanner(file))
        {
            list.getChildren().clear();
            while(inputFile.hasNextLine())
            {
                library.add(new Book(downloadLink(new URL("https://www.googleapis.com/books/v1/volumes?q=isbn:" + inputFile.nextLine()))));
                lineCount++;
            }
        }
        
        //  Sets the initial contents of the book list. 
        fillList();

        //  Sets the stage to the initial layout.
        stage.setTitle("Bookstore");
        stage.setScene(initializeNodes());
        stage.show();
    }
    
    //  Overloaded method which, displays the information for a specific ISBN, 
    //  which is passed to it. 
    public void fillList(String isbn) throws IOException
    {
        isbn = isbn.replaceAll("[^\\d]", "");
        
        //  Empties the current contents of the VBox "list".
        list.getChildren().clear();
        
        //  Stores the entire JSON data file, which is received from "downloadLink()",
        //  into a JSONObject.
        JSONObject json = downloadLink(new URL("https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn));

        //  If results are found, then they are diplayed in the "list" VBox.
        if(json.getInt("totalItems") != 0)
        {   
            //  Adds this book to the library array list.
            library.add(new Book(json));
            
            //  Adds the information pane for this book to the VBox "list".
            list.getChildren().addAll(library.get(library.size() - 1).getPane(), new Line(0, 0, (780), 0));
            
            //  Sets the appropriate actions which occur when the "Add to Cart" 
            //  Button for this book is pressed.
            library.get(library.size() - 1).btAdd.setOnAction(e ->
            {   
                for(int j = 0; j < shoppingCart.size(); j++)
                {
                    //  Checks if this book already exists in the cart. If it does,
                    //  then it's quantity in the cart is updated.
                    if(shoppingCart.get(j).getIsbn().equals(library.get(library.size() - 1).getIsbn()))
                    {
                        library.get(library.size() - 1).setQuantity(shoppingCart.get(j).getQuantity());
                        library.get(library.size() - 1).setInCart(true);
                    }
                }
                
                //  Adds this book to the cart if it's quantity is not 0.
                if(library.get(library.size() - 1).getTFQuantity() != 0 && !library.get(library.size() - 1).getInCart())
                {
                    library.get(library.size() - 1).setInCart(true);
                    shoppingCart.add(library.get(library.size() - 1));
                }
                
                //  Update's the quantity of this book to equal the value in the 
                //  quantity Text Field.
                shoppingCart.get(shoppingCart.size() - 1).setQuantity(shoppingCart.get(shoppingCart.size() - 1).getTFQuantity() + shoppingCart.get(shoppingCart.size() - 1).getQuantity());

                try 
                {
                    //  Updates the cart with the new/updated items.
                    fillCart();
                } 
                catch (ParseException ex) 
                {

                }
            });
        }
        else
        {
            //  Returns an error message if the Google Books API returns 0 results.
            message("No Results Found",
                    "We were unable to find any titles with this ISBN.\n"
                  + "Please ensure that you entered the desired ISBN correctly.");
        }
    }
    
    //  Overloaded method which, reloads the information inside the library.txt 
    //  file. 
    public void fillList()
    {
        //  Empties the current contents of the VBox "list".
        list.getChildren().clear();
        
        populateList();        
    }
    
    //  Stores the JSON data file into a string object.
    public JSONObject downloadLink(URL url) throws IOException
    {
        //  Defines and initializes all needed variables and objects.
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder buffer = new StringBuilder();
        int read;
        char[] chars = new char[1024];
        
        //  Uses a string builder to store the enitre JSON data file into a single
        //  one line string, which is then returned. 
        while ((read = reader.read(chars)) != -1)
        {    
            buffer.append(chars, 0, read); 
        }
        String jsonUrl = buffer.toString();
        return new JSONObject(jsonUrl);
    }
    
    //  Fills the VBox list with the information on the books stored in the 
    //  library.txt file.
    public void populateList()
    {
        
        //  Cycles through the arrayList finding the information on the books 
        //  stored in the library.txt file.
        for (int i = 0; i < lineCount; i++) 
        {   
            final int ii = i;
            list.getChildren().add(library.get(i).getPane());
            list.getChildren().add(new Line(0, 0, (780), 0));
            
            //  Sets the appropriate actions which occur when the "Add to Cart" 
            //  Button for this book is pressed.
            library.get(ii).btAdd.setOnAction(e ->
            {   
                //  Checks if this book already exists in the cart. If it does,
                //  then it's quantity is increased by the amount in it's quantity
                //  Text Field.
                for (Book shoppingCart1 : shoppingCart)
                {
                    if(library.get(ii).getIsbn().equals(shoppingCart1.getIsbn()))
                    {
                        shoppingCart1.setQuantity(shoppingCart1.getTFQuantity() + shoppingCart1.getQuantity());
                    }
                }              
                
                //  Adds this book to the cart if it's quantity is not 0.
                if(library.get(ii).getTFQuantity() != 0 && !library.get(ii).getInCart())
                {
                    library.get(ii).setInCart(true);
                    library.get(ii).setQuantity(library.get(ii).getTFQuantity());
                    shoppingCart.add(library.get(ii));
                }

                try 
                {
                    //  Updates the cart with the new/updated items.
                    fillCart();
                } 
                catch (ParseException ex) 
                {

                }
            });
        }  
    }
    
    //  Fills the cartList and shoppingCart with the appropriate books.
    public void fillCart() throws ParseException
    {
        //  Clears all books from the "cartList" VBox.
        while(cartList.getChildren().contains(cartList.lookup("HBox")))
        {
            cartList.getChildren().remove(cartList.lookup("HBox"));
        }
        
        //  Resets the subtotal to 0.
        subtotal = 0.00;
        
        //  Clears the shoppingCart arrayList.
        shoppingCart.clear();
        
        //  Adds all of the items in the library, which should be in the cart, to
        //  the shoppingCart arrayList.
        for (int i = 0; i < library.size(); i++)
        {
            final int ii = i;
            
            if(library.get(ii).getInCart())
            {
                shoppingCart.add(library.get(ii));
            }
        }

        //  Cycles through the shoppingCart arrayList and displays all items in 
        //  the cartList VBox.
        for (int j = 0; j < shoppingCart.size(); j++) 
        {
            final int jj = j;
            
            if (shoppingCart.get(jj).getQuantity() != 0) 
            {
                Label title = new Label(shoppingCart.get(jj).getTitle());
                Label price = new Label("  " + fmt1.format(shoppingCart.get(jj).getPrice()) + "\t   ");
                Label quantity = new Label(Integer.toString(shoppingCart.get(jj).getQuantity()));
                CheckBox cbSelect = new CheckBox();

                cbSelect.setOnAction(e1 -> 
                {
                    shoppingCart.get(jj).setIsSelected(cbSelect.isSelected()); 
                });
                title.setMinWidth(200);
                title.setMaxWidth(200);
                title.setWrapText(true);
                title.setPadding(new Insets(5,0,5,0));
                price.setPadding(new Insets(5,0,5,0));
                quantity.setPadding(new Insets(5,0,5,0));

                cartList.getChildren().add(new HBox(cbSelect, title, price, quantity));

                subtotal += shoppingCart.get(jj).getTotal(); 
            }  
            else
            {
                shoppingCart.remove(jj); 
            }
        }
        
        //  Sets the subtotal Label to the current subtotal.
        lblSubTotal.setText(fmt1.format(subtotal));
    }
    
    //  Builds the recipt, which will be displayed and/or saved.
    public void generateRecipt() throws ParseException 
    {
        //  Defines and initializes all necessary instance variables and objects
        Stage stage = new Stage();
        TextArea taRecipt = new TextArea();
        Button btSave = new Button("Save to File");
        VBox vbox = new VBox(19);
        HBox btArea = new HBox();
        Scene scene = new Scene(vbox, 320, 450);
        taRecipt.setMinWidth(300);
        taRecipt.setMinHeight(200);
        taRecipt.setEditable(false);
        
        //  Adds the recipt header to the recipt text area.
        taRecipt.setText("Title                                                        Price    Quantity\n"
                       + "---------------------------------------------------------------\n");
        
        //  Assures that the title is always seperated (by a certain number of 
        //  whitespaces) from the price. Also, if the title is more than 28 
        //  characters in length, then it will be truncated with the remaining
        //  characters being replaced with "..."
        for (int i = 0; i < shoppingCart.size(); i++)
        {
            String s = shoppingCart.get(i).getTitle();
            
            if(s.length() > 28)
            {
                s = s.substring(0, Math.min(s.length(), 28));
                s+= "...";
            }
            else
            {
                s = String.format("%-35s", shoppingCart.get(i).getTitle());
            }
            
            taRecipt.setText(taRecipt.getText() + s 
                    + "\t\t" + fmt1.format(shoppingCart.get(i).getPrice()) + 
                    "\t" + shoppingCart.get(i).getQuantity() + "\n");
        }
        
        //  Adds the recipt footer to the recipt TextArea. This includes the total
        //  and subtotal. 
        taRecipt.setText(taRecipt.getText() + 
                         "---------------------------------------------------------------\n" +
                         "                                                                 Subtotal: " + fmt1.format(subtotal)+ "\n" +
                         "                                                                       Total: " + fmt1.format(subtotal * TAX_RATE));
        
        //  Saves the recipt when the save Button is pressed.
        btSave.setOnAction(e -> 
        {
            try 
            {
                saveRecipt(taRecipt.getText());
            }
            catch (FileNotFoundException ex) 
            {
                
            }
        });
        
        //  Puts all nodes and panes in their respective containers and formats.
        btArea.setAlignment(Pos.CENTER);
        btArea.getChildren().addAll(btSave);
        taRecipt.setMinHeight(400);
        vbox.getChildren().addAll(taRecipt, btArea);
        
        stage.setResizable(false);
        stage.setTitle("Recipt");
        stage.setScene(scene);
        stage.show();
    }
    
    //  Saves the generated recipt to a file.
    public void saveRecipt(String taRecipt) throws FileNotFoundException
    {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        
        //Restricts the fileChooser to only save to .txt documents.
        fileChooser.getExtensionFilters().addAll
        (
            new FileChooser.ExtensionFilter("Text Documents (*.txt)", "*.txt")
        );
        
        //Opens the file chooser and stores the file path that the user selects
        //into the file object.
        File file = fileChooser.showSaveDialog(stage);
        
        //Writes the data to the recipt file.
        try (PrintWriter output = new PrintWriter(file)) 
        {
            output.print(taRecipt);

            //Closes the file.
            output.close();
        }
    }
    
    //  This method is used to display a warning message with the specified 
    //  message text and title.
    public void message(String title, String message)
    {
        GridPane gridPane = new GridPane();
        Button btOk = new Button("OK");
        Label lblmessage = new Label(message);
        
        lblmessage.setFont(Font.font("ArialBlack",FontWeight.BOLD, 12));
        lblmessage.setAlignment(Pos.CENTER);
        
        GridPane.setHalignment(lblmessage, HPos.CENTER);
        GridPane.setHalignment(btOk, HPos.CENTER);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(10);
        gridPane.add(lblmessage, 0, 0);
        gridPane.add(btOk, 0, 1);
                
        Stage stage = new Stage();
        btOk.setOnAction(e -> stage.close());
        
        stage.setTitle(title);
        stage.setScene(new Scene(gridPane, 450, 150));
        stage.show();
    }
    
    //  Displays information about the program.
    public void aboutMessage()
    {
        HBox hbox = new HBox(20);
        Label title = new Label("BookWorld");
        Label versionNum = new Label("Version: 1.10");
        Label developer = new Label("Developed by: Jon Knight");
        Label description = new Label();
        ImageView books = new ImageView(new Image("file:books.png"));
        
        title.setAlignment(Pos.CENTER);
        title.setFont(Font.font("Caspian", FontWeight.BOLD, 25));
        versionNum.setFont(Font.font("Caspian", FontWeight.BOLD, 12));
        developer.setFont(Font.font("Caspian", FontWeight.BOLD, 15));
        description.setText
        (
            "This program functions as a bookstore, complete with a shopping cart\n"
          + "and the ability to search for an individual book by ISBN. All of the\n"
          + "results and information of each book is retrieved via the Google Books\n"
          + "API."
        );
        books.setFitWidth(200);
        books.setPreserveRatio(true);
        
        hbox.getChildren().addAll(books, new VBox(8, title, versionNum, developer, description));
        
        Stage stage = new Stage();               
        stage.setTitle("About");
        stage.setScene(new Scene(hbox, 600, 200));
        stage.show();
    }
    
    //  This method sets the format, style, font etc for all nodes and panes used
    //  in this program.
    public Scene initializeNodes()
    {
        Scene scene = new Scene(window, 1150, 700);
        
        menuExit.setOnAction(e -> System.exit(0));
        menuAbout.setOnAction(e -> aboutMessage());
        menuFile.getItems().add(menuExit);
        menuHelp.getItems().add(menuAbout); 
        menuBar.getMenus().addAll(menuFile, menuHelp);
        menuBar.prefWidthProperty().bind(scene.widthProperty());
        
        cartLbl.getChildren().add(lblCart);
        cartLbl.setAlignment(Pos.CENTER);
        cartLbl.setMaxWidth(300);
        
        lblCart.setFont(Font.font("ArialBlack",FontWeight.BOLD, 13));
        lblCart.setPadding(new Insets(7,12,5,12));
        
        scrollPane.prefHeightProperty().bind(scene.heightProperty());
        scrollPane.setContent(list);
        
        cartBtns.prefHeightProperty().bind(scene.heightProperty());        
        cartBtns.setMaxHeight(30);
        cartBtns.setMaxWidth(320);
        cartBtns.setPadding(new Insets(12,0,12,0));
        cartBtns.setAlignment(Pos.BOTTOM_CENTER);
        cartBtns.getChildren().addAll(checkout, remove);
        
        cartHeader.add(new Label("      Title                                                        "), 0, 0);
        cartHeader.add(new Label("Price    "), 1, 0);
        cartHeader.add(new Label("Quantity"), 2, 0);
        cartList.getChildren().addAll(cartHeader, new Line(0,0,317,0));
        
        spCart.prefHeightProperty().bind(scene.heightProperty());
        spCart.setMaxWidth(320);
        spCart.setContent(cartList);
        
        cartFooter.add(new Line(0,0,317,0), 0, 0);
        cartFooter.add(new HBox(new Label("Subtotal: "),lblSubTotal), 0, 1);
        GridPane.setHalignment(cartFooter.lookup("HBox"), HPos.RIGHT);
        
        checkout.setOnAction(e ->
        {
            if(subtotal != 0.00)
            {
                try 
                {
                    generateRecipt();
                }
                catch (ParseException ex) 
                {
                    
                }
            }
            else
            {
                message("Empty Cart","The cart is empty!\nPlease ensure there is something in the cart, before attempting to checkout.");
            }
        });
        
        remove.setOnAction(e ->
        {
            for (int i = 0; i < library.size(); i++)
            {
                if(library.get(i).getIsSeleted())
                {
                    try 
                    {
                        subtotal -= library.get(i).getTotal();
                        library.get(i).setQuantity(0);
                        library.get(i).setInCart(false);
                        library.get(i).setIsSelected(false);
                    } 
                    catch (ParseException ex) 
                    {
                        
                    }
                }
            }
                
            try 
            {
                fillCart();
            }
            catch (ParseException ex) 
            {
                
            }
        });

        btHome.setVisible(false);        
        btHome.setOnAction(e -> 
        {
            btHome.setVisible(false);
            fillList();
        });
        
        tfSearch.setMaxHeight(10);
        searchBar.setPadding(new Insets(3, 0, 3, 0));

        btSearch.setOnAction(e ->
        {
            if(!tfSearch.getText().equals(""))
            {
                btHome.setVisible(true);
            }
            
            if(!tfSearch.getText().equals(""))
            {
                try 
                {
                    fillList(tfSearch.getText());
                } 
                catch (IOException ex) 
                {
                    
                }
            }
        });
        
        cart.getChildren().addAll(cartLbl,spCart,cartFooter,cartBtns);
        
        window.setCenter(new VBox(searchBar, scrollPane));
        window.setRight(cart);
        window.setTop(menuBar);
        
        return scene;
    }
    
    public static void main(String[] args)
    {
       launch(args);
    }
}
