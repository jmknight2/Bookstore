package Bookstore;

import java.text.NumberFormat;
import java.text.ParseException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.json.*;

/*
 * Class: CITC1311 Programming II 
 * Instructor: Gitti Negahban
 * Programmer: Jonathan Knight
 * Description: This class is utilized by it's driver "ShoppingCart"
 */

public class Book 
{
    //  Defines and intializes all necessary variables and objects.
    Button btAdd = new Button("Add To Cart");
    
    private int quantity;
    private boolean inCart = false, isSelected = false;
    private String isbn;
    private NumberFormat fmt1 = NumberFormat.getCurrencyInstance();
    private Label lblBookTitle = new Label();    
    private Label lblAuthor = new Label();
    private Label lblPrice = new Label();
    private BorderPane borderPane = new BorderPane();
    private GridPane gridPane = new GridPane();
    private GridPane paneLeft = new GridPane();
    private HBox quantityPane = new HBox();
    private HBox info = new HBox();
    private TextField tfQuantity = new TextField("1");
    private TextArea taDescription = new TextArea();
    
    //  No argument constructor.
    public Book()
    {
        
    }
    
    //  Processes the JSONOject which is passed to it. This object is then used 
    //  to assign the appropriate values to all variables and objects contained
    //  within this class.
    public Book(JSONObject json) throws JSONException
    {            
        //  Extracts the "items" array from the JSONObject.
        JSONArray items = json.getJSONArray("items");      
        
        //  Extracts the "volumeInfo" object from the JSONArray at index 0.
        JSONObject volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo");
        
        //  Extracts the string with the tag "identifier" from the JSONArray 
        //  "industryIdentifiers" which is held inside the "volumeInfo" JSONObject.
        isbn = volumeInfo.getJSONArray("industryIdentifiers").getJSONObject(0).getString("identifier");
        
        //  Extracts the string with the tag "title" from the JSONObject "volumeInfo". 
        lblBookTitle.setText(volumeInfo.getString("title"));
        
        //  Extracts the string from index 0 of the JSONArray "authors", which 
        //  is stored inside the "volumeInfo" JSONObject.
        lblAuthor.setText(volumeInfo.getJSONArray("authors").getString(0));
        
        //  Extracts the string with the tag "description" from the JSONObject 
        //  "volumeInfo".
        taDescription.setText(volumeInfo.getString("description"));
        
        //  Extracts the JSONObject with the tag "saleInfo" from the items JSONArray
        JSONObject saleInfo = items.getJSONObject(0).getJSONObject("saleInfo");

        //  Determines if this book's info contains a price. If it does, then 
        //  the price Label is updated with this value. If it doesn't, a default
        //  price of $9.95 is stored in it's place. 
        try
        {
            lblPrice.setText(fmt1.format(saleInfo.getJSONObject("retailPrice").getDouble("amount")));
        }
        catch(JSONException ex)
        {
            lblPrice.setText(fmt1.format(9.95));
        }       

        //  Converts the ISBN to ISBN 13 format, if it isn't already.
        if(isbn.length() == 10)
        {
            isbn = "978" + isbn;
        }
        
        //  Extracts the string with the tag "tumbnail" from the JSONObject 
        //  "imageLinks" which is held inside the "volumeInfo" JSONObject.
        String imgUrl = volumeInfo.getJSONObject("imageLinks").getString("thumbnail");

        //  Initializes the format, style, font, etc for all nodes and panes in 
        //  this class.
        initializeNodes(imgUrl);
    }
    
    
    //***************************************
    //  Below, are all the necessary getters.
    //***************************************
    public String getTitle()
    {
        return lblBookTitle.getText();
    }
    
    public String getAuthor()
    {
        return lblAuthor.getText();
    }
    
    public String getDescription()
    {
        return taDescription.getText();
    }
    
    public double getPrice() throws ParseException
    {
        return fmt1.parse(lblPrice.getText()).doubleValue();
    }
    
    public String getIsbn()
    {
        return isbn;
    }
    
    public int getTFQuantity()
    {
        return Integer.parseInt(tfQuantity.getText());
    }
    
    public int getQuantity()
    {
        return quantity;
    }
    
    public boolean getInCart()
    {
        return inCart;
    }
    
    public boolean getIsSeleted()
    {
        return isSelected;
    }
    
    public double getTotal() throws ParseException
    {
        return quantity * fmt1.parse(lblPrice.getText()).doubleValue();
    }
    
    public BorderPane getPane()
    {
        return borderPane;
    }
    
    //***************************************
    //  Below, are all the necessary setters.
    //***************************************
    public void setTitle(String title)
    {
        lblBookTitle.setText(title);
    }
    
    public void setAuthor(String author)
    {
        lblAuthor.setText(author);
    }
    
    public void setDescription(String description)
    {
        lblAuthor.setText(description);
    }
    
    public void setPrice(double price)
    {
        lblPrice.setText(Double.toString(price));
    }
    
    public void setIsbn(String isbn)
    {
        this.isbn = isbn;
    }
    
    public void setTFQuantity(int tfQuantity)
    {
        this.tfQuantity.setText(Integer.toString(tfQuantity));
    }
    
    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }
    
    public void setInCart(boolean inCart)
    {
        this.inCart = inCart;
    }
    
    public void setIsSelected(boolean isSelected)
    {
        this.isSelected = isSelected;
    }
    
    public void setPane(BorderPane borderPane)
    {
        this.borderPane = borderPane;
    }
    
    //  Initializes the format, style, font, etc for all nodes and panes in 
    //  this class.
    private void initializeNodes(String imgUrl) throws NullPointerException
    {
        Image image = new Image(imgUrl);

        ImageView imgThumb = new ImageView();
        imgThumb.setImage(image);
        imgThumb.setFitWidth(125);
        imgThumb.setPreserveRatio(true);
        
        Image cartIcon = new Image("file:cart_icon.png");
        ImageView imgCart = new ImageView();
        imgCart.setImage(cartIcon);
        imgCart.setFitWidth(25);
        imgCart.setPreserveRatio(true);
        
        taDescription.setMaxWidth(400);
        taDescription.setMinWidth(400);
        taDescription.setMaxHeight(100);
        taDescription.setMinHeight(100);
        taDescription.setWrapText(true);
        tfQuantity.setMaxWidth(30);
        btAdd.setMinWidth(75);
        btAdd.setGraphic(imgCart);
        lblAuthor.setPadding(new Insets(0,50,0,0));
        lblBookTitle.setFont(Font.font("ArialBlack",FontWeight.BOLD, 17));
        lblPrice.setFont(Font.font("ArialBlack",FontWeight.BOLD, 13));
        quantityPane.getChildren().addAll(new Label("Quantity: "), tfQuantity);
        quantityPane.setAlignment(Pos.TOP_RIGHT);
        info.getChildren().addAll(new Label("Author: "), lblAuthor, new Label("Price: "),lblPrice);
        gridPane.add(lblBookTitle, 1, 0);
        gridPane.add(info, 1, 1);
        gridPane.add(taDescription, 1, 2);
        gridPane.setHgap(5);
        gridPane.setVgap(10);
        paneLeft.add(quantityPane, 0, 0);
        paneLeft.add(btAdd, 0, 1);
        
        borderPane.setLeft(paneLeft);
        borderPane.setCenter(imgThumb);
        borderPane.setRight(gridPane);
        BorderPane.setMargin(gridPane, new Insets(12,12,12,12));
        BorderPane.setMargin(paneLeft, new Insets(12,12,12,12));
        
    }
}
