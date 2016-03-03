
package Cubos3d;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
//import javafx.scene.control.Alert;
//import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
//import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineBuilder;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Cubos3D extends Application {
 
// Parametros de largura e altura da zona de desenho
private final double sceneWidth = 1200;
private final double sceneHeight = 600;
private double mouseXold = 0;
private double mouseYold = 0;

// Variaveis para movimentar o ecrã com o rato
private double mousePosX;
private double mousePosY;
private double mouseOldX;
private double mouseOldY;
private double mouseDeltaX;
private double mouseDeltaY;

final Cam camOffset = new Cam();
final Cam cam = new Cam();

// Nx,Ny e Nz corresponde ao número de nós da rede em cada uma das direcções.
private double nX=2,nY=2,nZ=2;
    
Color cores[]  = {Color.BLUE,Color.AQUAMARINE,Color.PALEGREEN,Color.YELLOWGREEN,
                    Color.GREEN,Color.LIGHTYELLOW,Color.YELLOW,Color.ORANGE,Color.RED,Color.DARKRED,Color.SILVER};
 
// Matriz com os intervalos de valores pa a escala de core activa
//double valorCores[] ={1,2,3,4,5,6,7,8,9,10};
// Matriz com as 3 escalas de cores V1, V2 e V3;



HashMap gridList = new HashMap();
PhongMaterial materiais[] = new PhongMaterial[11];
public int editElement = 1; //Qual o valor que está a ser mudado 1 = V1; 2 = V2; 3=V3
public double valueEdit = 0; // O valor que deve ser introduzido na celula
public int codigoMaterial=5; // O valor do codigo de cor(matrial) que está a ser mudade
public boolean editaPlanosFlag=false; // Se está a editar os planos
public boolean editFunction = false; // Se está a editar os valores de uma celula

// Os diferentes menus
VBox varPrecisionMovements, parametrosRede;
VBox edicaoParcialRede;
HBox symbolMenu;
MenuBar teclasMenus = new MenuBar();

// O painel com todos os elementos
//VBox controlPanel;
TabPane controlPanel = new TabPane();

BorderPane borderPane;

HBox listOfGrids = new HBox();
int editingGrid = 0;

public String dialogs[] = new String[100];

public static class constantes {
    public final double g_universal = 6.674287*Math.pow(10,-11);
    public final double g_refx=0;
    public final double g_refy=0;
    public final double g_refz=-9.80000;
    public final double densidade_ref=3200;
}

public static constantes listaConstantes = new constantes();

class Cam extends Group {
     Translate t  = new Translate();
     Translate p  = new Translate();
     Translate ip = new Translate();
     Rotate rx = new Rotate();
     { rx.setAxis(Rotate.X_AXIS); }
     Rotate ry = new Rotate();
     { ry.setAxis(Rotate.Y_AXIS); }
     Rotate rz = new Rotate();
     { rz.setAxis(Rotate.Z_AXIS); }
     Scale s = new Scale();
     public Cam() { super(); getTransforms().addAll(t, p, rx, rz, ry, s, ip); }
 }   
   
class ponto {
        double x,y,z;
}
	
class celula {
    ponto p; 							// coordenadas do centro da celula
    double V1,V2,V3;   
    int x,y,z;                                                      // Caso se pretenda corresponde aos indices da celula
    ponto lado;							// volume da celula
    boolean visivel = true;
    int indice;
    public celula() {
       p = new ponto();
       lado = new ponto();
    }
    @Override 
    public String toString() {
    String texto = "celula {\n" +
           "P(x)="+p.x+" P(y)="+p.y+" P(z)="+p.z+
           "\nV1="+V1+" V2="+V2+" V3="+V3+
           "\nL(x)="+lado.x+" L(y)="+lado.y+" L(z)="+lado.z+
           "\n x:"+x+" y:"+y+" z:"+z+
           "\nVisivel="+visivel+"\n}";
   return texto;
   }
}       
        
class rede {        
    public int redeNX,redeNY,redeNZ;
    public double redeMinX,redeMinY,redeMinZ;
    public double redeMaxX,redeMaxY,redeMaxZ;
    public double redeMinV1,redeMinV2,redeMinV3;
    public double redeMaxV1,redeMaxV2,redeMaxV3;
    public String name;
    public double reduce =.3;
    // Matriz com os intervalos de valores pa a escala de core activa
    public double valorCores[] ={1,2,3,4,5,6,7,8,9,10};
    // Matriz com as 3 escalas de cores V1, V2 e V3;
    public double escalasCores[][]={valorCores,valorCores,valorCores};           

    private HashMap lista = new HashMap();                              // O HashMap vai ser do tipo (n, celula)

    private rede(String nameGrid) {
        name = nameGrid;
        }

    @Override 
    public String toString() {
    String texto = "Name:"+name+
            "\nElements ="+lista.size()+
            "\nNx:"+redeNX+"\tNy:"+redeNY+"\tNz:"+redeNZ+
            "\nminV1:"+redeMinV1+" \tminV2:"+redeMinV2+" \tminV3:"+redeMinV3+
            "\nmaxV1:"+redeMaxV1+"\tmaxV2:"+redeMaxV2+"\tmaxV3:"+redeMaxV3;
    return texto;
    }

    public void setName(String newName) {
        name = newName;
    }

    public void addCelula(int index, celula c) {
        verificaLimites(c);
        c.indice=index;
        lista.put(index,c);
        }

    public Object devolveCelula(int index) {
        return lista.get(index);
    }
    private void recalculaLimites() {
    //***************************
    // Valores de coordenadas do centro
    //***************************
    celula c;
    for(int j = 0; j < lista.size(); j++) {
        c = (celula) lista.get(j);
        if(j==0) {
            redeMinX=c.p.x; redeMaxX=c.p.x;
            redeMinY=c.p.y; redeMaxY=c.p.y;
            redeMinZ=c.p.z; redeMaxZ=c.p.z;          
            redeMinV1=c.V1; redeMaxV1=c.V1;
            redeMinV2=c.V2; redeMaxV2=c.V2;
            redeMinV3=c.V3; redeMaxV3=c.V3; 
            redeNX = c.x+1; redeNY=c.y+1; redeNZ=c.z+1;
            } else {
            for(int i = 0; i< lista.size(); i++) {
                if(c.p.x<redeMinX) redeMinX=c.p.x;
                if(c.p.x>redeMaxX) redeMaxX=c.p.x;
                if(c.p.y<redeMinY) redeMinY=c.p.y;
                if(c.p.y<redeMaxY) redeMaxY=c.p.y;
                if(c.p.z<redeMinZ) redeMinZ=c.p.z;
                if(c.p.z<redeMaxZ) redeMaxZ=c.p.z;
                if(c.V1<redeMinV1) redeMinV1=c.V1;
                if(c.V1>redeMaxV1) redeMaxV1=c.V1;
                if(c.V2<redeMinV2) redeMinV2=c.V2;
                if(c.V2>redeMaxV2) redeMaxV2=c.V2;
                if(c.V3<redeMinV3) redeMinV3=c.V3;
                if(c.V3>redeMaxV3) redeMaxZ=c.V3;  
                if(redeNX < c.x+1) redeNX = c.x+1;
                if(redeNY < c.y+1) redeNY = c.y+1;
                if(redeNZ < c.z+1) redeNZ = c.z+1;                                   
            }
        }
    }
    }
    private void verificaLimites(celula c) {
        //***************************
        // Valores de coordenadas do centro
        //***************************
        if(lista.isEmpty()) {
            redeMinX=c.p.x; redeMaxX=c.p.x;
            redeMinY=c.p.y; redeMaxY=c.p.y;
            redeMinZ=c.p.z; redeMaxZ=c.p.z;          
            redeMinV1=c.V1; redeMaxV1=c.V1;
            redeMinV2=c.V2; redeMaxV2=c.V2;
            redeMinV3=c.V3; redeMaxV3=c.V3; 
            redeNX = c.x+1; redeNY=c.y+1; redeNZ=c.z+1;
        } else {
            for(int i = 0; i< lista.size(); i++) {
                if(c.p.x<redeMinX) redeMinX=c.p.x;
                if(c.p.x>redeMaxX) redeMaxX=c.p.x;
                if(c.p.y<redeMinY) redeMinY=c.p.y;
                if(c.p.y<redeMaxY) redeMaxY=c.p.y;
                if(c.p.z<redeMinZ) redeMinZ=c.p.z;
                if(c.p.z<redeMaxZ) redeMaxZ=c.p.z;
                if(c.V1<redeMinV1) redeMinV1=c.V1;
                if(c.V1>redeMaxV1) redeMaxV1=c.V1;
                if(c.V2<redeMinV2) redeMinV2=c.V2;
                if(c.V2>redeMaxV2) redeMaxV2=c.V2;
                if(c.V3<redeMinV3) redeMinV3=c.V3;
                if(c.V3>redeMaxV3) redeMaxZ=c.V3;  
                if(redeNX < c.x+1) redeNX = c.x+1;
                if(redeNY < c.y+1) redeNY = c.y+1;
                if(redeNZ < c.z+1) redeNZ = c.z+1;                                   
            }
        }
    }
}

@Override
public void start(final Stage primaryStage) {      

    // Read the dialog texts
     loadDialogs();

     // Create the materials (baed on colors)
    createMaterials();

    // Define the menu menu
    teclasMenus = defineMenu();
    
    // Create the tab bar with the different Layouts and functions
    createTabs();
    
    
    camOffset.getChildren().add(cam);
    resetCam();

    final SubScene scene = new SubScene(camOffset, sceneWidth, sceneHeight,true, SceneAntialiasing.DISABLED);

    scene.setFill(Color.BLACK);
    scene.setCamera(new PerspectiveCamera());

    primaryStage.setTitle(dialogs[67]);
         
    
    // ******************************************
    // Center the camera to view all the scene    
    double halfSceneWidth  = scene.getWidth()/2.0;
    double halfSceneHeight = scene.getHeight()/2.0;
    cam.p.setX(halfSceneWidth);
    cam.ip.setX(-halfSceneWidth);
    cam.p.setY(halfSceneHeight);
    cam.ip.setY(-halfSceneHeight);

    frameCam(primaryStage, scene);

    // Define the movements of the mouse
    definePalco(scene);
    


    // The complete stage is made of this borderPane
    borderPane = new BorderPane(scene, teclasMenus, null, controlPanel, null);

    Scene scene2 = new Scene(borderPane);
    defineTeclasGlobais(primaryStage, scene2,  scene);
     
    //*******************************************
    // Set Scene and Show the stage
    primaryStage.setScene(scene2);
    primaryStage.show();
 }

//*******************************************
// The diferent tabs in the stage 
private void createTabs() {
    
    varPrecisionMovements = definePrecisionMovement();
    varPrecisionMovements.setAlignment(Pos.CENTER);
    parametrosRede = createEntryFields();
    parametrosRede.setAlignment(Pos.CENTER);
    symbolMenu = createSymbolMenu();
    symbolMenu.setAlignment(Pos.CENTER);
    listOfGrids = createListGrids();
        
    // Define the differnt tabs
    Tab tabMovimentos = new Tab();
    tabMovimentos.setText(dialogs[49]);
    tabMovimentos.setContent(varPrecisionMovements);
    controlPanel.getTabs().add(tabMovimentos);

    Tab tabCriaRede = new Tab();
    tabCriaRede.setText(dialogs[50]);
    tabCriaRede.setContent(parametrosRede);
    controlPanel.getTabs().add(tabCriaRede);
    
    Tab tabSymbols = new Tab();
    tabSymbols.setText(dialogs[52]);
    tabSymbols.setContent(symbolMenu);
    controlPanel.getTabs().add(tabSymbols);   
    
    Tab tabListOfGrids = new Tab();
    tabListOfGrids.setText(dialogs[53]);
    tabListOfGrids.setContent(listOfGrids);
    controlPanel.getTabs().add(tabListOfGrids);    
    
}

// ****************************
// Create the materials

private void createMaterials() {

    for(int i = 0; i <10;i++) {
        materiais[i] = new PhongMaterial();
        materiais[i].setDiffuseColor(cores[i]);
    }

}

// #########################################
// Criar os Menus
// #########################################
private MenuBar defineMenu() {
    MenuBar menuBar = new MenuBar();
  
    // Menu File
    Menu menuFile = new Menu(dialogs[3]);
    MenuItem readGrid = new MenuItem(dialogs[4]);
    readGrid.setOnAction(new EventHandler<ActionEvent>() {
        public void handle(ActionEvent t) {
            // Read the grid
            // Must verify where it should be located
            selectGrid();
            gridList.put(editingGrid,readGrid());  
/*            
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(dialogs[8]);
            alert.setHeaderText(dialogs[9]);
            alert.setContentText(((rede) gridList.get(editingGrid)).toString());
            alert.show();
  */          
            // Update the list of Grids
            updateListOfGrids();                  
            
            // Redraw the elements
            desenhaCubo();
            
            // Update the symbols lists
            for(int j = 0 ; j < 10; j++) {
                ((TextField) ((HBox) ((ListView) symbolMenu.getChildren().get(1)).getItems().get(j)).getChildren().get(1)).setText(String.format(Locale.ENGLISH,"%.9f", ((rede) gridList.get(editingGrid)).escalasCores[editElement-1][j]));
                } 
        }
    });  
    
    MenuItem writeGrid = new MenuItem(dialogs[5]);
    writeGrid.setOnAction(new EventHandler<ActionEvent>() {
        public void handle(ActionEvent t) {
            gravaGrelha(((rede) gridList.get(editingGrid)));
        }
    });    
    
    MenuItem exitMenu = new MenuItem(dialogs[6]);
    exitMenu.setOnAction(new EventHandler<ActionEvent>() {
        public void handle(ActionEvent t) {
            System.exit(0);        
        }
    });    
    menuFile.getItems().addAll(readGrid,new SeparatorMenuItem(),writeGrid,new SeparatorMenuItem(), exitMenu);

    // Menu Model
    Menu menuModel = new Menu(dialogs[7]);
    MenuItem modelGrid = new MenuItem(dialogs[8]);
    modelGrid.setOnAction(new EventHandler<ActionEvent>() {
        public void handle(ActionEvent t) {
        chooseGrid();
        desenhaCubo();        
        }
    });    
 
    menuModel.getItems().addAll(modelGrid, new SeparatorMenuItem());
    
    menuBar.getMenus().addAll(menuFile, menuModel);

    return menuBar;
}
 
// #########################################        
// Define o que fazer quando prime uma tecla
// Tem que estar aqui pois o pressionar das 
//     teclas depende das 2 subcenas
// Isto é do scene2     
// #########################################
private void defineTeclasGlobais(Stage primaryStage,Scene scene2, SubScene scene) {

    scene2.setOnKeyPressed(event -> {
        String keyPressed = event.getText();
        System.out.println(keyPressed);

         if(event.isControlDown() && event.isShiftDown()) { 
             resetCam();
             frameCam(primaryStage,scene);   
         } 
         if(keyPressed.equals("+")) {
            cam.t.setX(cam.t.getX()+1);
        }
         if(keyPressed.equals("/")) {
             VBox xx = (VBox) symbolMenu.getChildren().get(0);
             CheckBox ch = (CheckBox) xx.getChildren().get(0);
         }
         if(keyPressed.equals("-")) {
            System.out.println(((rede) gridList.get(editingGrid)).toString()); 
         }                 

        if(keyPressed.equals("*")) {
            for(int i = 0; i < ((rede) gridList.get(editingGrid)).lista.size(); i++) {
                 celula cell = (celula)((rede) gridList.get(editingGrid)).devolveCelula(i);
                 System.out.println("i:"+i+"\n"+cell);
             }

        }
    
    });    
}
 
private VBox defineParametrosRede() {
    Slider btReduce = createSliderPercentage(30);

    btReduce.valueProperty().addListener(new ChangeListener<Number>() {
        public void changed(ObservableValue<? extends Number> ov,
            Number old_val, Number new_val) {
                ((rede) ((rede) gridList.get(editingGrid))).reduce = (double)new_val/100;
                desenhaCubo();
        }
    });
    VBox vbox3 = new VBox(10, btReduce, createFixedAxis() );
    vbox3.setPadding(new Insets(10, 10, 10, 10));
    vbox3.setAlignment(Pos.CENTER);
    return vbox3;
} 
 
//******************************************
// Define os movimentos de precisão
//******************************************
private VBox definePrecisionMovement() {
        
    Slider translactionXSlider = createSlider(0);
    Image imageTRight = new Image(getClass().getResourceAsStream("tRight32.png"));
    Button buttonTRight = new Button();
    buttonTRight.setGraphic(new ImageView(imageTRight));
    buttonTRight.setOnAction(new EventHandler<ActionEvent>() {
        @Override 
        public void handle(ActionEvent e) {
               cam.t.setX(cam.t.getX()+translactionXSlider.valueProperty().getValue());    
        }
    });  

    Slider translactionYSlider = createSlider(0);
    Image imageTLeft = new Image(getClass().getResourceAsStream("tUp32.png"));
    Button buttonTLeft = new Button();
    buttonTLeft.setGraphic(new ImageView(imageTLeft));
    buttonTLeft.setOnAction(new EventHandler<ActionEvent>() {
        @Override 
        public void handle(ActionEvent e) {
               cam.t.setY(cam.t.getY()+translactionYSlider.valueProperty().getValue());    
        }
    });  

    Slider translactionZSlider = createSlider(0);
    Image imageTDepth = new Image(getClass().getResourceAsStream("tZ32.png"));
    Button buttonTDepth = new Button();
    buttonTDepth.setGraphic(new ImageView(imageTDepth));

    buttonTDepth.setOnAction(new EventHandler<ActionEvent>() {
        @Override 
        public void handle(ActionEvent e) {
               cam.t.setZ(cam.t.getZ()+translactionZSlider.valueProperty().getValue());    
        }
    }); 

     HBox hboxTranslaction = new HBox(10, new Label(dialogs[55]), translactionXSlider,buttonTRight,
     new Label(dialogs[57]), translactionYSlider,buttonTLeft,
     new Label(dialogs[58]), translactionZSlider,buttonTDepth);
     hboxTranslaction.setPadding(new Insets(10, 10, 10, 10));
     hboxTranslaction.setAlignment(Pos.CENTER);     
     
    Slider rotationXSlider = createSlider(0);
    Image imageRX = new Image(getClass().getResourceAsStream("rX32.png"));
    Button buttonRX = new Button();
    buttonRX.setGraphic(new ImageView(imageRX));

    buttonRX.setOnAction(new EventHandler<ActionEvent>() {
        @Override 
        public void handle(ActionEvent e) {
            double rxAngle = cam.rx.getAngle();
            cam.rx.setAngle(rxAngle +rotationXSlider.valueProperty().getValue());    
        }
    });      

    Slider rotationYSlider = createSlider(0);
    Image imageRY = new Image(getClass().getResourceAsStream("rY32.png"));
    Button buttonRY = new Button();
    buttonRY.setGraphic(new ImageView(imageRY));

    buttonRY.setOnAction(new EventHandler<ActionEvent>() {
        @Override 
        public void handle(ActionEvent e) {
            double ryAngle = cam.ry.getAngle();
            cam.ry.setAngle(ryAngle +rotationYSlider.valueProperty().getValue());    
        }
    });
     
    Slider rotationZSlider = createSlider(0);
    Image imageRZ = new Image(getClass().getResourceAsStream("rZ32.png"));
    Button buttonRZ = new Button();
    buttonRZ.setGraphic(new ImageView(imageRZ));

    buttonRZ.setOnAction(new EventHandler<ActionEvent>() {
        @Override 
        public void handle(ActionEvent e) {
            double rzAngle = cam.rz.getAngle();
            cam.rz.setAngle(rzAngle +rotationZSlider.valueProperty().getValue());    
        }
    });
     
     HBox hbox2 = new HBox(10, new Label(dialogs[56]), rotationXSlider,buttonRX,
     new Label(dialogs[57]), rotationYSlider,buttonRY,
     new Label(dialogs[58]), rotationZSlider,buttonRZ);
     hbox2.setPadding(new Insets(10, 10, 10, 10));
     hbox2.setAlignment(Pos.CENTER);

     VBox vbox= new VBox(30, hboxTranslaction, hbox2);
     
     return vbox ;
}  

// #########################################           
// Se clicar os movimentos do rato 
// #########################################
private void definePalco(SubScene scene) {

    scene.setOnMouseMoved(event-> {
        if(event.isControlDown() && editFunction  ){
            Node picked = event.getPickResult().getIntersectedNode();
            if(null != picked) {
               if(picked.getClass().getSimpleName().equals("Box")) {
                   Box pickedMesh= (Box)picked;
                   celula celulaEscolhida = (celula)pickedMesh.getUserData();
                   //System.out.println("peguei:"+celulaEscolhida.V1);
                    pickedMesh.setMaterial(materiais[codigoMaterial]);     
                    switch(editElement) {
                        case 1: celulaEscolhida.V1=valueEdit; break;
                        case 2: celulaEscolhida.V2=valueEdit; break;
                        case 3: celulaEscolhida.V3=valueEdit; break;
                    }
                    // Actualiza cubo e rede
                    pickedMesh.setUserData(celulaEscolhida);
                    ((rede) gridList.get(editingGrid)).addCelula(celulaEscolhida.indice,celulaEscolhida);
                }
            }
        }
    });
// #########################################

// #########################################
// Faz zoom quando utilizamos a tecla de scrool do rato
// #########################################
    scene.setOnScroll(event -> {
        double scale = cam.s.getX();
        double newScale = scale + event.getDeltaY()*.001;
        cam.s.setX(newScale);
        cam.s.setY(newScale);
        cam.s.setZ(newScale);
    });
// #########################################
         
// #########################################
// Rotação com base na posição do rato
// #########################################
    
    scene.setOnMouseDragged(event->  {
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = event.getX();
        mousePosY = event.getY();
        mouseDeltaX = mousePosX - mouseOldX;
        mouseDeltaY = mousePosY - mouseOldY;

         if (event.isAltDown() && event.isShiftDown() && event.isPrimaryButtonDown()) {
             double rzAngle = cam.rz.getAngle();
             cam.rz.setAngle(rzAngle - mouseDeltaX);
         }
         else if (event.isAltDown() && event.isPrimaryButtonDown()) {
             double ryAngle = cam.ry.getAngle();
             cam.ry.setAngle(ryAngle - mouseDeltaX);
             double rxAngle = cam.rx.getAngle();
             cam.rx.setAngle(rxAngle + mouseDeltaY);
         }
         else if (event.isAltDown() && event.isMiddleButtonDown()) {
             double tx = cam.t.getX();
             double ty = cam.t.getY();
             cam.t.setX(tx + mouseDeltaX);
             cam.t.setY(ty + mouseDeltaY);
         }                     
    });

// #########################################    
} 
 
private Slider createSlider(double value) {
    Slider slider = new Slider(-30, 30, value);
    slider.setShowTickMarks(true);
    slider.setMajorTickUnit(10);
    slider.setMinorTickCount(10);
    slider.setBlockIncrement(10);   
    slider.setShowTickLabels(true);
    return slider;
}
  
private VBox createFixedAxis() {
    final ToggleGroup groupAxis = new ToggleGroup();
    final VBox grupo = new VBox();
    final HBox valueSelected = new HBox();
    valueSelected.setPadding(new Insets(20,0,20,0));
    grupo.setPadding(new Insets(20,20,10,20));
    final Button editaPlanos= new Button(dialogs[36]);
    
    // Ativa e desactiva a edição de um eixo
    editaPlanos.setOnAction(new EventHandler<ActionEvent>() {
        @Override 
        public void handle(ActionEvent e) {
            System.out.println("Porra");
            if( editaPlanosFlag ) {
                editaPlanosFlag = false;
                valueSelected.getChildren().get(0).setVisible(false);
                valueSelected.getChildren().get(1).setVisible(false);
                valueSelected.getChildren().get(2).setVisible(false);
                grupo.getChildren().get(1).setVisible(false);
                grupo.getChildren().get(2).setVisible(false);
                grupo.getChildren().get(3).setVisible(false);


                for(int i = 0; i < ((rede) gridList.get(editingGrid)).lista.size(); i++) {
                    celula cell = (celula)((rede) gridList.get(editingGrid)).devolveCelula(i);
                    cell.visivel=true;
                    ((rede) gridList.get(editingGrid)).addCelula(i, cell);
                }
                desenhaCubo();
                }
            else {
                editaPlanosFlag = true;
                valueSelected.getChildren().get(0).setVisible(true);
                valueSelected.getChildren().get(1).setVisible(true);
                valueSelected.getChildren().get(2).setVisible(true);
                grupo.getChildren().get(1).setVisible(true);
                grupo.getChildren().get(2).setVisible(true);
                grupo.getChildren().get(3).setVisible(true);
            }
        }
    });  
    
    // Os Botões que definem os eixos X, Y e Z
    RadioButton  tb1 = new RadioButton (dialogs[37]);
    tb1.setToggleGroup(groupAxis);
    tb1.setSelected(true);
    tb1.setVisible(false);
    tb1.setUserData("X");  
    
    RadioButton  tb2 = new RadioButton (dialogs[38]);
    tb2.setToggleGroup(groupAxis);
    tb2.setVisible(false);
    tb2.setUserData("Y");
    
    RadioButton  tb3 = new RadioButton (dialogs[39]);
    tb3.setToggleGroup(groupAxis);
    tb3.setVisible(false);
    tb3.setUserData("Z");
    
   
    // Operações a efetuar quando escolhe um eixo diferente
    groupAxis.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
    @Override
    public void changed(ObservableValue<? extends Toggle> ov,
        Toggle toggle, Toggle new_toggle) {        

        // Actualiza o valor do slider com os valores da Rede
        Slider slider;
        int valor=1; int valorRede = 0;
        switch((String)groupAxis.getSelectedToggle().getUserData()) {
            case "X": valor =((rede) gridList.get(editingGrid)).redeNX; break;
            case "Y": valor =((rede) gridList.get(editingGrid)).redeNY; break;
            case "Z": valor =((rede) gridList.get(editingGrid)).redeNZ; break;
        }
        slider = (Slider) grupo.getChildren().get(2);

        slider.setMax(valor-1);
        // Unidade principal é um quarto dos valores
        slider.setMajorTickUnit(valor>=4?(int)valor/4:1);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(1); 
        slider.setMinorTickCount(1);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setValue(valorRede);
        slider.setVisible(true);
        slider.valueProperty().addListener(new ChangeListener<Number>() {
             @Override
             public void changed(ObservableValue<? extends Number> ov,
             Number old_val, Number new_val) {
             int valorRede=0;
             double init = (double) System.currentTimeMillis();
             for(int i = 0; i < ((rede) gridList.get(editingGrid)).lista.size(); i++) {
                celula cell = (celula)((rede) gridList.get(editingGrid)).devolveCelula(i);
                switch((String)groupAxis.getSelectedToggle().getUserData()) {
                    case "X": valorRede = cell.x; break;
                    case "Y": valorRede = cell.y; break;
                    case "Z": valorRede = cell.z; break;
                }           
                if( valorRede != new_val.intValue()) {
                    cell.visivel=false;
                    ((rede) gridList.get(editingGrid)).addCelula(i, cell);
                } else {
                    cell.visivel=true;
                    ((rede) gridList.get(editingGrid)).addCelula(i, cell);                       
                }
             }
            double end = (double) System.currentTimeMillis();
            System.out.println("Fim Desenho Plano:"+(end-init));
            desenhaCubo();
            }
        });               
        // Actualiza o slider e a etiqueta
        grupo.getChildren().remove(3);
        grupo.getChildren().remove(2);

        grupo.getChildren().add(slider);
        Label etiqueta = new Label(String.format(" %2.0f",slider.getValue()));
        etiqueta.setVisible(true);
        grupo.getChildren().add(etiqueta);

        editaPlanosFlag = true;

        // Actualiza a rede para mostrar apenas o plano definido
        int x=0; // Começa sempre por mostra o plano 0
        for(int i = 0; i < ((rede) gridList.get(editingGrid)).lista.size(); i++) {
            celula cell = (celula) ((rede) gridList.get(editingGrid)).devolveCelula(i);
        switch((String)groupAxis.getSelectedToggle().getUserData()) {
            case "X": valorRede = cell.x; break;
            case "Y": valorRede = cell.y; break;
            case "Z": valorRede = cell.z; break;
        }           
            if( valorRede != x) {
                cell.visivel=false;
                ((rede) gridList.get(editingGrid)).addCelula(i, cell);
            } else {
                cell.visivel=true;
                ((rede) gridList.get(editingGrid)).addCelula(i, cell);                       
            }
        }
        desenhaCubo();
    }
        
 });

    // A ideia é criar o slider com os valores minimo e máximo dos indices da rede.
    //Slider slider = new Slider(0,((rede) gridList.get(editingGrid)).redeNX-1, 0);
    Slider slider = new Slider(0,1, 0);

    slider.setShowTickMarks(true);
    slider.setMajorTickUnit(1);

    slider.setMinorTickCount(1);
    slider.setBlockIncrement(1); 
    slider.setSnapToTicks(true);
    slider.setShowTickLabels(true);
    slider.setVisible(false);
    slider.valueProperty().addListener(new ChangeListener<Number>() {
       @Override
       public void changed(ObservableValue<? extends Number> ov,
       Number old_val, Number new_val) {
       int valorRede=0;
       for(int i = 0; i < ((rede) gridList.get(editingGrid)).lista.size(); i++) {
           celula cell = (celula)((rede) gridList.get(editingGrid)).devolveCelula(i);
            switch((String)groupAxis.getSelectedToggle().getUserData()) {
                case "X": valorRede = cell.x; break;
                case "Y": valorRede = cell.y; break;
                case "Z": valorRede = cell.z; break;
            }           
            if( valorRede != new_val.intValue()) {
                cell.visivel=false;
                ((rede) gridList.get(editingGrid)).addCelula(i, cell);
            } else {
                cell.visivel=true;
                ((rede) gridList.get(editingGrid)).addCelula(i, cell);                       
            }
       }
       // Actualiza a etiqueta
       grupo.getChildren().remove(3);
       Label etiqueta = new Label(String.format(" %2.0f",slider.getValue()));
       etiqueta.setVisible(true);
       grupo.getChildren().add(etiqueta);
       desenhaCubo();
       }
   });

    grupo.getChildren().add(editaPlanos);
    valueSelected.getChildren().addAll(tb1,tb2,tb3);
    grupo.getChildren().add(valueSelected);
    grupo.getChildren().add(slider);
    grupo.setAlignment(Pos.CENTER);
    Label etiqueta = new Label(String.format(" %2.2f",slider.getValue()));
    etiqueta.setVisible(false);
    grupo.getChildren().add(etiqueta);
    
    return grupo;
}
 
private Slider createSliderPercentage(double value) {
    Slider slider = new Slider(0, 100, value);
    slider.setShowTickMarks(true);
    slider.setMajorTickUnit(25);
    slider.setMinorTickCount(10);
    slider.setBlockIncrement(10);   
    slider.setShowTickLabels(true);
    slider.setMaxWidth(200);
    return slider;
}
 
public void resetCam() {
    cam.t.setX(0.0);
    cam.t.setY(0.0);
    cam.t.setZ(0.0);
    cam.rx.setAngle(0.0);
    cam.ry.setAngle(0.0);
    cam.rz.setAngle(0.0);
    cam.s.setX(1.25);
    cam.s.setY(1.25);
    cam.s.setZ(1.25);


    cam.p.setX(0.0);
    cam.p.setY(0.0);
    cam.p.setZ(0.0);

    cam.ip.setX(0.0);
    cam.ip.setY(0.0);
    cam.ip.setZ(0.0);

    final Bounds bounds = cam.getBoundsInLocal();
    final double pivotX = bounds.getMinX() + bounds.getWidth() / 2;
    final double pivotY = bounds.getMinY() + bounds.getHeight() / 2;
    final double pivotZ = bounds.getMinZ() + bounds.getDepth() / 2;

    cam.p.setX(pivotX);
    cam.p.setY(pivotY);
    cam.p.setZ(pivotZ);

    cam.ip.setX(-pivotX);
    cam.ip.setY(-pivotY);
    cam.ip.setZ(-pivotZ);
 }
 
//=========================================================================
// CubeSystem.frameCam
//=========================================================================
public void frameCam(final Stage stage, final SubScene scene) {
    setCamOffset(camOffset, scene);
    // cam.resetTSP();
    setCamPivot(cam);
    setCamTranslate(cam);
    setCamScale(cam, scene);
}

//=========================================================================
// CubeSystem.setCamOffset
//=========================================================================
private void setCamOffset(final Cam camOffset, final SubScene scene) {
    double width = scene.getWidth();
    double height = scene.getHeight();
    camOffset.t.setX(width/2.0);
    camOffset.t.setY(height/2.0);
}

//=========================================================================
// setCamScale
//=========================================================================
private void setCamScale(final Cam cam, final SubScene scene) {
    final Bounds bounds = cam.getBoundsInLocal();
    final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
    final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
    final double pivotZ = bounds.getMinZ() + bounds.getDepth()/2;

    double width = scene.getWidth();
    double height = scene.getHeight();

    double scaleFactor;
    double scaleFactorY = 1.0;
    double scaleFactorX = 1.0;
    if (bounds.getWidth() > 0.0001) {
        scaleFactorX = width / bounds.getWidth(); // / 2.0;
    }
    if (bounds.getHeight() > 0.0001) {
        scaleFactorY = height / bounds.getHeight(); //  / 1.5;
    }
    if (scaleFactorX > scaleFactorY) {
        scaleFactor = scaleFactorY;
    } else {
        scaleFactor = scaleFactorX;
    }
    cam.s.setX(scaleFactor);
    cam.s.setY(scaleFactor);
    cam.s.setZ(scaleFactor);
}

//=========================================================================
// setCamPivot
//=========================================================================
private void setCamPivot(final Cam cam) {
    final Bounds bounds = cam.getBoundsInLocal();
    final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
    final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
    final double pivotZ = bounds.getMinZ() + bounds.getDepth()/2;
    cam.p.setX(pivotX);
    cam.p.setY(pivotY);
    cam.p.setZ(pivotZ);
    cam.ip.setX(-pivotX);
    cam.ip.setY(-pivotY);
    cam.ip.setZ(-pivotZ);
}

//=========================================================================
// setCamTranslate
//=========================================================================
private void setCamTranslate(final Cam cam) {
    final Bounds bounds = cam.getBoundsInLocal();
    final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
    final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
    cam.t.setX(-pivotX);
    cam.t.setY(-pivotY);
}
 
  
private VBox createEntryFields() {
        
    HBox panel1 = new HBox();
    HBox panel2 = new HBox();
    VBox panel = new VBox();
    panel2.setPadding(new Insets(20,0,20,0));
    
    String[] labelStrings = {
        dialogs[18],
        dialogs[19],
        dialogs[20],
        dialogs[21],
        dialogs[22],
        dialogs[23],
        dialogs[24],
        dialogs[25],
        dialogs[26]
    };
 
    Label[] labels = new Label[labelStrings.length];
    TextField[] fields = new TextField[labelStrings.length];
    int fieldNum = 0;
 
    // Cria os campos de texto e guarda-os na lista
    TextField nPontosX = new TextField(dialogs[27]);
    nPontosX.setMaxWidth(40);
    fields[fieldNum++] = nPontosX;
        
    TextField nPontosY = new TextField(dialogs[28]);
    nPontosY.setMaxWidth(40);
    fields[fieldNum++] = nPontosY;

    TextField nPontosZ = new TextField(dialogs[29]);
    nPontosZ.setMaxWidth(40);
    fields[fieldNum++] = nPontosZ;        

    TextField p1X = new TextField(dialogs[30]);
    p1X.setPrefWidth(90);
    fields[fieldNum++] = p1X;

    TextField p1Y = new TextField(dialogs[31]);
    p1Y.setMaxWidth(90);
    fields[fieldNum++] = p1Y;

    TextField p2X = new TextField(dialogs[32]);
    p2X.setMaxWidth(90);
    fields[fieldNum++] = p2X;     

    TextField p2Y = new TextField(dialogs[33]);
    p2Y.setMaxWidth(90);
    fields[fieldNum++] = p2Y;   
    
    TextField p1Z = new TextField(dialogs[34]);
    p1Z.setMaxWidth(90);
    fields[fieldNum++] = p1Z;  
    
    TextField p2Z = new TextField(dialogs[35]);
    p2Z.setMaxWidth(90);
    fields[fieldNum++] = p2Z;  


    // Cria os campos
    // Campos relativos aos nós
    for (int i = 0; i < 3; i++) {
        labels[i] = new Label(labelStrings[i]);
        labels[i].setLabelFor(fields[i]);

        panel1.getChildren().add(labels[i]);
        panel1.getChildren().add(fields[i]);

    }
    
    // Campos para as coordenadas
    // A segunda linha do painel
    
    for (int i = 3; i < labelStrings.length; i++) {
        labels[i] = new Label(labelStrings[i]);
        labels[i].setLabelFor(fields[i]);

        panel2.getChildren().add(labels[i]);
        panel2.getChildren().add(fields[i]);

    }
        
        
    panel1.setAlignment(Pos.TOP_CENTER);    
    panel2.setAlignment(Pos.TOP_CENTER);  
    Button btNovaRede = new Button(dialogs[68]);
    btNovaRede.setStyle(dialogs[0]);
    btNovaRede.setOnAction(new EventHandler<ActionEvent>() {
    @Override 
        public void handle(ActionEvent e) {
            final int nX=Integer.parseInt(fields[0].getText());    
            final int nY=Integer.parseInt(fields[1].getText());    
            final int nZ=Integer.parseInt(fields[2].getText());
            final double p1X=Double.parseDouble(fields[3].getText());  
            final double p1Y=Double.parseDouble(fields[4].getText());              
            final double p2X=Double.parseDouble(fields[5].getText());              
            final double p2Y=Double.parseDouble(fields[6].getText());  
            final double p1Z=Double.parseDouble(fields[7].getText());  
            final double p2Z=Double.parseDouble(fields[8].getText());              

            final double V1=0;             
            final double V2=0;
            final double V3=0;

            selectGrid();
            gridList.put(editingGrid, criaRede(nX,nY,nZ,p1X,p1Y,p2X,p2Y,p1Z,p2Z,V1,V2,V3));
            
            // Update the list of Grids
            updateListOfGrids();
            
            // Redraw with the new elements
            desenhaCubo();
            
            // Update The Symbols Texts
            updateTextBoxSymbols();
        }
    });
        
        HBox botoesComando = new HBox();
        botoesComando.getChildren().add(btNovaRede);
        botoesComando.setAlignment(Pos.TOP_CENTER);
        
        panel.getChildren().addAll(panel1,panel2,botoesComando);

        return panel;
    }
private void updateTextBoxSymbols() {
    for(int j = 0 ; j < 10; j++) {
        ((TextField) ((HBox) 
                ((ListView) symbolMenu.getChildren().get(1)).getItems().get(j))
                .getChildren().get(1))
                .setText(String.format(Locale.ENGLISH,"%.9f", ((rede) gridList.get(editingGrid)).escalasCores[editElement-1][j]));
    } 
}
       
private void updateListOfGrids() {
    ObservableList obsList = ((ListView) listOfGrids.getChildren().get(0)).getItems();
    HBox hbGrid = (HBox) obsList.get(editingGrid);
    Label lbGrid = (Label) hbGrid.getChildren().get(1);
    CheckBox cbGrid = (CheckBox) hbGrid.getChildren().get(0);
    cbGrid.setSelected(true);
    lbGrid.setText(((rede) gridList.get(editingGrid)).name);
    hbGrid.getChildren().set(1, lbGrid);
    hbGrid.getChildren().set(0, cbGrid);
    obsList.set(editingGrid,hbGrid);

}

private HBox createEditValue() {
    HBox menuEdit = new HBox(10);
    menuEdit.setAlignment(Pos.CENTER);

    
    CheckBox editCheckBox =new CheckBox(dialogs[69]);
    TextField textEditValue = new TextField(dialogs[70]);  
    textEditValue.setVisible(false);
    textEditValue.setMaxWidth(100);
    textEditValue.setOnAction(new EventHandler<ActionEvent>() {
        @Override 
            public void handle(ActionEvent e) {
                
                   codigoMaterial= returnColor(Double.parseDouble(textEditValue.getText()),editingGrid);
                   valueEdit = Double.parseDouble(textEditValue.getText());
                   
            }
        }); 
    editCheckBox.setSelected(false);
    editCheckBox.setOnAction(new EventHandler<ActionEvent>() {
        @Override 
            public void handle(ActionEvent e) {
               if(editCheckBox.isSelected()) {
                   textEditValue.setVisible(true);
                   codigoMaterial= returnColor(Double.parseDouble(textEditValue.getText()),editingGrid);
                   valueEdit = Double.parseDouble(textEditValue.getText());  
                   editFunction = true;
                    }
               else {
                   textEditValue.setVisible(false);
                   editFunction = false;
               }
            }
        }); 
    menuEdit.getChildren().addAll(editCheckBox,textEditValue);
    return menuEdit;
}

private HBox createEditElement() {
    HBox menuEdit = new HBox(20);
    menuEdit.setAlignment(Pos.CENTER);
    
    RadioButton cbV1 =new RadioButton(dialogs[14]);
    cbV1.setSelected(true);
    cbV1.setUserData(1);
    
    RadioButton cbV2 =new RadioButton(dialogs[15]);
    cbV2.setSelected(false);
    cbV2.setUserData(2);

    RadioButton cbV3 =new RadioButton(dialogs[16]);
    cbV3.setSelected(false); 
    cbV3.setUserData(3);
        
    ToggleGroup tgValue = new ToggleGroup();
    cbV1.setToggleGroup(tgValue);
    cbV2.setToggleGroup(tgValue);
    cbV3.setToggleGroup(tgValue);
    
    tgValue.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
      public void changed(ObservableValue<? extends Toggle> ov,
          Toggle old_toggle, Toggle new_toggle) {
        if (tgValue.getSelectedToggle() != null) {
          editElement = (int) tgValue.getSelectedToggle().getUserData();
          for(int j = 0 ; j < 10; j++) {
            ((TextField) ((HBox) ((ListView) symbolMenu.getChildren().get(1)).getItems().get(j)).getChildren().get(1)).setText(String.format(Locale.ENGLISH,"%.9f", ((rede) gridList.get(editingGrid)).escalasCores[editElement-1][j]));
            }             
          desenhaCubo();
        }
      }
    });
    menuEdit.getChildren().addAll(cbV1,cbV2,cbV3);
    return menuEdit;
}

private HBox createSymbolMenu() {
    
    // This is the Layout for this tab
    // It has two columns (leftColumn and listSymbols)
    HBox caixaCores = new HBox();

    // These are for the ListView of the symbology
    CheckBox valores[]=new CheckBox[11];
    TextField[] textoValorCores = new TextField[11];
    ListView listSymbols = new ListView();
    listSymbols.setMaxHeight(160);    
    HBox codigo[] = new HBox[11];
     
   //These are for editing the values of the grid variables
    HBox editValue = new HBox();
    HBox editVariable = new HBox();

    editValue.setAlignment(Pos.TOP_CENTER);
    editVariable.setAlignment(Pos.CENTER);

    editValue = createEditValue();
    editVariable = createEditElement();
        
    // This is the Left Column
    VBox leftColumn = new VBox();
     
    // Create the sub menu for editing the grid
    leftColumn.getChildren().addAll(editValue, editVariable, defineParametrosRede());
    leftColumn.setAlignment(Pos.CENTER);
    caixaCores.getChildren().add(leftColumn);
    
    // CReate the elements for the ListView (Colors of Symbols and values)
    for(int i = 0; i <11;i++) {
        int valor=i;
        codigo[i] = new HBox();
        
        // THe CheckBox of the diferente color symbols
        valores[i] = new CheckBox(i<10?""+(i+1):dialogs[45]);
        String texto = String.format( "#%02X%02X%02X",
            (int)( cores[i].getRed() * 255 ),
            (int)( cores[i].getGreen() * 255 ),
            (int)( cores[i].getBlue() * 255 ) );
            
        valores[i].setStyle(dialogs[1] + texto+";");
        valores[i].setPrefWidth(40);        
        valores[i].setSelected(true);
        valores[i].setOnAction(new EventHandler<ActionEvent>() {
        @Override 
        public void handle(ActionEvent e) {
           codigoMaterial=valor;
           double valorCelula=0;

            if(valores[valor].isSelected()) {
                 for(int j = 0; j < ((rede) gridList.get(editingGrid)).lista.size(); j++){
                     celula cell = (celula) ((rede) gridList.get(editingGrid)).devolveCelula(j);
                     switch(editElement) {
                         case 1: valorCelula=cell.V1; break;
                         case 2: valorCelula=cell.V2; break;
                         case 3: valorCelula=cell.V3; break;
                     }
                     if(valor == returnColor(valorCelula,editingGrid) || (returnColor(valorCelula,editingGrid) == 10 && valor ==10)) cell.visivel=true; 
                     ((rede) gridList.get(editingGrid)).addCelula(j, cell);
                 }            
            } else {
                 for(int j = 0; j < ((rede) gridList.get(editingGrid)).lista.size(); j++){
                     celula cell = (celula) ((rede) gridList.get(editingGrid)).devolveCelula(j);
                     switch(editElement) {
                         case 1: valorCelula=cell.V1; break;
                         case 2: valorCelula=cell.V2; break;
                         case 3: valorCelula=cell.V3; break;
                     }
                     if(valor == returnColor(valorCelula,editingGrid) || (returnColor(valorCelula,editingGrid) == 10 && valor ==10)) cell.visivel=false; 
                     ((rede) gridList.get(editingGrid)).addCelula(j, cell);
                 }  
              }     
            desenhaCubo();
            }
        });

        
        // The textField with the values for each color
        textoValorCores[i] = new TextField(i<10?""+i:"-");
        textoValorCores[i].setStyle(dialogs[2]);
        textoValorCores[i].setPrefSize(160,valores[i].getHeight()-5);
        textoValorCores[i].setOnAction(  new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent e) {
            //Aqui vai fazer o sort da array e voltar a mostrar os valores
            double novaListaValores[] = new double[11];
            for(int j = 0 ; j < 10; j++) {
                novaListaValores[j]=Double.parseDouble(((TextField) ((HBox) ((ListView) symbolMenu.getChildren().get(1)).getItems().get(j)).getChildren().get(1)).getText());
            }
            
            Arrays.sort(novaListaValores,0,10);
            
            for(int j = 0 ; j < 10; j++) {
                textoValorCores[j].setText(String.format(Locale.ENGLISH,"%.9f", novaListaValores[j]));
            }            
            ((rede) gridList.get(editingGrid)).valorCores = novaListaValores;
            ((rede) gridList.get(editingGrid)).escalasCores[editElement-1]= novaListaValores;
            desenhaCubo();            }
        });
        textoValorCores[i].setEditable(true);
        codigo[i].getChildren().addAll(valores[i],textoValorCores[i]);
  
        //caixaCores.getChildren().add(codigo[i]);
        listSymbols.getItems().add(codigo[i]);
    }
    
    // The Button for Classify
    String text =dialogs[0];
    String text2 = dialogs[71];
    Button btClassify = new Button(dialogs[72]);
    btClassify.setStyle(text);
    btClassify.onMouseEnteredProperty().set(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
       btClassify.setStyle( text+text2);
               }
    });
    btClassify.onMouseExitedProperty().set(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
       btClassify.setStyle(text);
               }
    });   
    btClassify.setOnAction(new EventHandler<ActionEvent>() {
    @Override 
    public void handle(ActionEvent e) {
        double listaValores[] = new double[10];
        ((rede) gridList.get(editingGrid)).recalculaLimites();
        switch(editElement) {
            case 1:
                for(int i = 0; i <10;i++) {
                    listaValores[i] = ((rede) gridList.get(editingGrid)).redeMinV1+i*(((rede) gridList.get(editingGrid)).redeMaxV1-((rede) gridList.get(editingGrid)).redeMinV1)/9;
                }
                ((rede) gridList.get(editingGrid)).valorCores = listaValores;
                ((rede) gridList.get(editingGrid)).escalasCores[0]= ((rede) gridList.get(editingGrid)).valorCores;
                break;
            case 2: 
                for(int i = 0; i <10;i++) {
                    listaValores[i] = ((rede) gridList.get(editingGrid)).redeMinV2+i*(((rede) gridList.get(editingGrid)).redeMaxV2-((rede) gridList.get(editingGrid)).redeMinV2)/9;
                }
                ((rede) gridList.get(editingGrid)).valorCores = listaValores;
                ((rede) gridList.get(editingGrid)).escalasCores[1]= ((rede) gridList.get(editingGrid)).valorCores;
                break;
            case 3: 
                for(int i = 0; i <10;i++) {
                    listaValores[i] = ((rede) gridList.get(editingGrid)).redeMinV3+i*(((rede) gridList.get(editingGrid)).redeMaxV3-((rede) gridList.get(editingGrid)).redeMinV3)/9;
                }                    
                ((rede) gridList.get(editingGrid)).valorCores = listaValores;
                ((rede) gridList.get(editingGrid)).escalasCores[2]= ((rede) gridList.get(editingGrid)).valorCores;
                break;
        }    
        
        // Update the labels after classifying the elements
        for (int i = 0; i < 10; i++) {
            HBox hSymbol = (HBox) ((ListView) symbolMenu.getChildren().get(1)).getItems().get(i);         
            TextField t = (TextField) hSymbol.getChildren().get(1);           
            t.setText(String.format(Locale.ENGLISH,"%.9f", ((rede) gridList.get(editingGrid)).escalasCores[editElement-1][i]));       

            hSymbol.getChildren().set(1, t);
           ((ListView) symbolMenu.getChildren().get(1)).getItems().set(i,hSymbol);
        }

        // Redraw the grids
        desenhaCubo();
        }
    });  
    
    // Save Scale Button
    Button btSaveScale = new Button(dialogs[47]);
    btSaveScale.setStyle(text);
    btSaveScale.onMouseEnteredProperty().set(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
       btSaveScale.setStyle( text+text2);
               }
    });
    btSaveScale.onMouseExitedProperty().set(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
       btSaveScale.setStyle(text);
               }
    });   
    btSaveScale.setOnAction(new EventHandler<ActionEvent>() {
        @Override 
        public void handle(ActionEvent e) {
            saveScale();
        }
    });
 
    // Load Scale Button
    Button btLoadScale = new Button(dialogs[48]);
    btLoadScale.setStyle(text);
    btLoadScale.onMouseEnteredProperty().set(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
       btLoadScale.setStyle( text+text2);
               }
    });
    btLoadScale.onMouseExitedProperty().set(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
       btLoadScale.setStyle(text);
               }
    });   
    btLoadScale.setOnAction(new EventHandler<ActionEvent>() {
        @Override 
        public void handle(ActionEvent e) {
            loadScale();
        }
    });
    
    // Add the ListView to the Layout
    caixaCores.getChildren().add(listSymbols);
    VBox vbButtonSymbol = new VBox();
    vbButtonSymbol.setAlignment(Pos.CENTER);
    vbButtonSymbol.getChildren().addAll(btClassify,btSaveScale,btLoadScale);
    caixaCores.getChildren().add(vbButtonSymbol);
    
    return caixaCores;
}

// #########################################
// Desenha os eixos	
// #########################################
private void desenhaEixos() {

    Line blackLineX = LineBuilder.create()
        .startX(-150)
        .startY(0)
        .endX(150)
        .endY(0)
        .fill(Color.BLUE)
        .stroke(Color.BLUE)
        .strokeWidth(2.0f)
        .translateY(0)
        .build();       
       cam.getChildren().add(blackLineX);
       final Text textX = new Text(0, 0, dialogs[59]);
       textX.setFill(Color.WHITE);
       textX.setTranslateX(150);
       cam.getChildren().add(textX);
       
   Line blackLineY = LineBuilder.create()
        .startX(0)
        .startY(-150)
        .endX(0)
        .endY(150)
        .fill(Color.GREEN)
        .stroke(Color.GREEN)
        .strokeWidth(2.0f)
        .translateY(0)
        .build();
       //sceneRoot.getChildren().add(blackLineY);
       cam.getChildren().add(blackLineY);
       final Text textY = new Text(0, 0, dialogs[60]);
       textY.setFill(Color.WHITE);
       textY.setRotationAxis(Rotate.Z_AXIS);
       textY.setRotate(90);
       textY.setTranslateY(150);
       cam.getChildren().add(textY);      
       
  Line blackLineZ = LineBuilder.create()
        .startX(-150)
        .startY(0)
        .endX(150)
        .endY(0)
        .fill(Color.WHITE)
        .stroke(Color.WHITE)
        .strokeWidth(2.0f)
        .build();
        
       blackLineZ.setRotationAxis(Rotate.Y_AXIS);
       blackLineZ.setRotate(90);
       cam.getChildren().add(blackLineZ);
       
       final Text textZ = new Text(0, 0, dialogs[61]);
       textZ.setFill(Color.WHITE);
       textZ.setTranslateZ(150);
       textZ.setRotationAxis(Rotate.Y_AXIS);
       textZ.setRotate(270);  
       cam.getChildren().add(textZ);
}

private void desenhaCubo() {
    double init = System.currentTimeMillis();

    // Limpa a vista
    cam.getChildren().removeAll(cam.getChildren().sorted());

    desenhaEixos();
       
// #########################################
// Desenha os cubos
// #########################################       
ObservableList obsList = ((ListView) listOfGrids.getChildren().get(0)).getItems();
for(int j = 0; j < 5; j++) {
    HBox hbGrid = (HBox) obsList.get(j);
    CheckBox cbGrid = (CheckBox) hbGrid.getChildren().get(0);
    if( cbGrid.isSelected() && gridList.containsKey(j)) {
        double escalasCores[][] = ((rede) gridList.get(j)).escalasCores;

        for(int i = 0; i < ((rede) gridList.get(j)).lista.size(); i++) {
            celula cel = (celula) ((rede) gridList.get(j)).devolveCelula(i);
            if( cel.visivel) {
                Box cube = new Box(cel.lado.x*((rede) gridList.get(j)).reduce, cel.lado.y*((rede) gridList.get(j)).reduce, cel.lado.z*((rede) gridList.get(j)).reduce);
                /*
                switch(editElement) {
                    case 1:
                        valorCores = escalasCores[0];
                        break;
                    case 2:
                        valorCores = escalasCores[1];
                        break;
                    case 3:
                        valorCores = escalasCores[2];
                        break;
                }*/
                cube.setMaterial(materiais[returnColor(editElement==1?cel.V1:editElement==2?cel.V2:cel.V3,j)]);
                cube.setRotationAxis(Rotate.Y_AXIS);
                cube.setRotate(0);
                cube.setTranslateX((cel.p.x));
                cube.setTranslateY((cel.p.y));
                cube.setTranslateZ((cel.p.z));
                cube.setUserData(cel);                             
                cam.getChildren().add(cube);
            }
        }
    }
}
double fim= System.currentTimeMillis();
System.out.println("Tempo:"+(fim-init));

}   
    
private rede criaRede(int nx, int ny, int nz, double p1X, double p1Y, double p2X, double p2Y,
                    double p1Z, double p2Z, double V1, double V2, double V3) {

    double dx = (p2X-p1Y);
    double dy = (p2Y-p1Y);
    double dz = (p2Z-p1Z);

    double dcx = dx/nx;
    double dcy = dy/ny;
    double dcz = dz/nz;

    double cx = dcx/2;
    double cy = dcy/2;
    double cz = dcz/2;
    int contador = 0;
    
    rede redeNova = new rede(dialogs[54]+editingGrid);
    for(int i = 0; i < nx; i++) {
        for( int j = 0; j < ny; j++) {
                for( int k = 0; k < nz; k++) {
                        double a = p1X+cx+i*dcx;
                        double b = p1Y+cy+j*dcy;
                        double c = p1Z+cz+k*dcz;
                        ponto pontoCentral = new ponto();
                        
                        // Coordenadas do centro
                        pontoCentral.x = a; pontoCentral.y = b; pontoCentral.z = c;
                        ponto arestas = new ponto();
                        celula cel = new celula();
                        cel.p = pontoCentral;
                        cel.V1 = V1; cel.V2=V2; cel.V3= V3;
                        cel.x = i; cel.y=j; cel.z=k;
                        arestas.x = dcx; arestas.y = dcy; arestas.z = dcz;
                        cel.lado = arestas;
                        redeNova.addCelula(contador++,cel);
                        }
                }
        }
return redeNova;

}    
   
private void saveScale()  {
    Stage dialogo = new Stage();

    try
    {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(dialogs[65]);
    String separador = ",";

    String nomeFicheiro;
    nomeFicheiro =  fileChooser.showSaveDialog(dialogo).getAbsolutePath();

    FileWriter writer = new FileWriter(nomeFicheiro);
    String texto; 
        
    for(int j = 0; j < 3; j++) {   
        texto="";
        for(int i = 0; i < 10; i++) {
            texto=texto+((rede) gridList.get(editingGrid)).escalasCores[j][i]+separador;   
            }    
            writer.append(texto+"\n");
    }
    writer.flush();
    writer.close();
    }
    catch(IOException e) {
    } 
}

private void loadScale()  {
    Stage dialogo = new Stage();
    BufferedReader br;
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(dialogs[66]);
    String cvsSplitBy =",";
    String fileName;
    File file;
    try
    {
    file =  fileChooser.showOpenDialog(dialogo).getCanonicalFile();
    if(file.canRead()) {
        fileName = file.getCanonicalPath();
        int j = 0;
        br = new BufferedReader(new FileReader(fileName));
        String line;

        while( (line = br.readLine() ) !=null) {
            String[] values = line.split(cvsSplitBy);
            for (int i=0; i <10;i++)
               ((rede) gridList.get(editingGrid)).escalasCores[j][i] = Double.parseDouble(values[i]);
            j++;	
        }    
    }
    }
    catch(IOException e) {
    } 
}

private void loadDialogs()  {
    BufferedReader br;

    try
    {
    String fileName = ".\\dialogs.csv";
    int i = 0;
    // Read the file header and check if it is a real grid
    br = new BufferedReader(new FileReader(fileName));
    String line;

    while( (line = br.readLine() ) !=null) {
       dialogs[i++] = line;
       //System.out.println("i: "+i+" - "+dialogs[i-1]);
  }    
    
    }
    catch(IOException e) {
        e.printStackTrace();
    } 
}

private void gravaGrelha(rede redeInterna)  {
    Stage dialogo = new Stage();

      try
      {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setInitialFileName("*.csv");
      fileChooser.setTitle(dialogs[62]);
      String nomeFicheiro;
      nomeFicheiro =  fileChooser.showSaveDialog(dialogo).getAbsolutePath();

      FileWriter writer = new FileWriter(nomeFicheiro);

      writer.append("Id, I(x), I(y), I(z), P(x), P(y), P(z), L(x), L(y), L(z), V1, V2, V3\n");

      for(int i = 0; i < redeInterna.lista.size(); i++) {
          String texto = ""; 
          String separador = ",";
          celula cell = (celula) redeInterna.devolveCelula(i);
          texto=texto+cell.indice+separador;
          texto = texto + cell.x      +separador+cell.y      +separador+cell.z      +separador;
          texto = texto + cell.p.x    +separador+cell.p.y    +separador+cell.p.z    +separador;
          texto = texto + cell.lado.x +separador+cell.lado.y +separador+cell.lado.z +separador;
          texto = texto + cell.V1     +separador+cell.V2     +separador+cell.V3     +separador;        
          writer.append(texto+"\n");
          }    
      writer.flush();
      writer.close();
      }
      catch(IOException e)
      {
      e.printStackTrace();
      } 
}
 
private rede readGrid() {
rede redeInterna = new rede(dialogs[63]);
BufferedReader br;
String cvsSplitBy =",";
Stage dialogo = new Stage();
File file = new File("t");
int i=0;

try {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(dialogs[64]);
    String fileName;
    file =  fileChooser.showOpenDialog(dialogo).getCanonicalFile();
    fileName = file.getCanonicalPath();
// Read the file header and check if it is a real grid
    br = new BufferedReader(new FileReader(fileName));
    String line = br.readLine();
// Para garantir que o ficheiro é do tipo rede
    if( !line.equals("Id, I(x), I(y), I(z), P(x), P(y), P(z), L(x), L(y), L(z), V1, V2, V3") )
        System.exit(-1);
    
    while( (line = br.readLine() ) !=null) {
        String[] linha = line.split(cvsSplitBy);
        celula cel = new celula();
        //System.out.println("Linha:"+line+"\tlinha(4):"+linha[4]);
        int id = Integer.parseInt( linha[0] );
        cel.x = Integer.parseInt( linha[1] );
        cel.y = Integer.parseInt( linha[2] );
        cel.z = Integer.parseInt( linha[3] );        
        cel.p.x = Double.parseDouble( linha[4] );
        cel.p.y = Double.parseDouble( linha[5] );
        cel.p.z = Double.parseDouble( linha[6] );
        cel.lado.x = Double.parseDouble( linha[7] );
        cel.lado.y = Double.parseDouble( linha[8] );
        cel.lado.z = Double.parseDouble( linha[9] );
        cel.V1 = Double.parseDouble( linha[10] );
        cel.V2 = Double.parseDouble( linha[11] );
        cel.V3 = Double.parseDouble( linha[12] );
        redeInterna.addCelula(i, cel);       
        i++;	
    }    
} catch (FileNotFoundException e) {
        e.printStackTrace();
} catch (IOException e) {
        e.printStackTrace();
} 
redeInterna.name=file.getName();
return redeInterna;	
}
    
private int returnColor(double value, int gridNumber) {
    int returnValue = 10;
    for(int i = 0; i < 9; i++) 
        if( value >= ((rede) gridList.get(gridNumber)).escalasCores[editElement-1][i] && value <= ((rede) gridList.get(gridNumber)).escalasCores[editElement-1][i+1] ) 
            returnValue = i;

    return returnValue;
}

private double calculateVolume(ponto p1) { 					// devolve ponto, porque a distancia é um vector
    return p1.x*p1.y*p1.z;
}  
  
private ponto calculateDistance(ponto p1, ponto p2) { 					// devolve ponto, porque a distancia é um vector
    double dx,dy,dz;
    dx = Math.abs(p2.x-p1.x);
    dy = Math.abs(p2.y-p1.y);
    dz = Math.abs(p2.z-p1.z);
    ponto p = new ponto();
    p.x = dx; p.y = dy; p.z = dz;
    return p;
}
  
  
private HBox createListGrids() {
    HBox listGridsBox = new HBox();
    CheckBox bxGrid[] = new CheckBox[10];
    Label lbGrid[] = new Label[10];
    HBox hbGrid[] = new HBox[10];
    ObservableList data = FXCollections.observableArrayList();      
    // Define the proprieies of the list
    ListView listGrids = new ListView(data);
    listGrids.setMaxWidth(180);
    listGrids.setMaxHeight(120);
    // Define what actions will take when you select a new line of the grid
    listGrids.getSelectionModel().selectedIndexProperty().addListener(
        new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
          editingGrid = (int) newValue;
          //((TextArea) listGridsBox.getChildren().get(1)).setMinSize(200, 60);               
          if(gridList.containsKey(editingGrid) )
              ((TextArea) listGridsBox.getChildren().get(1)).setText(((rede) gridList.get(editingGrid)).toString());
              else
              ((TextArea) listGridsBox.getChildren().get(1)).setText(dialogs[41]);

        }
     });

    for(int i=0; i < 5; i++) {

      int valor = i;
      bxGrid[i] = new CheckBox();
      bxGrid[i].setOnAction(new EventHandler<ActionEvent>() {
      @Override 
      public void handle(ActionEvent e) {
          if(bxGrid[valor].isSelected()) {
              editingGrid = valor;          
              ((ListView) listOfGrids.getChildren().get(0)).getSelectionModel().select(valor);
          }
          if(gridList.containsKey(editingGrid))
              ((TextArea) listGridsBox.getChildren().get(1)).setText(((rede) gridList.get(editingGrid)).toString());
          else
              ((TextArea) listGridsBox.getChildren().get(1)).setText(dialogs[41]);

          desenhaCubo();
      }   
  });  
      lbGrid[i] = new Label(dialogs[40]);
      hbGrid[i] = new HBox();
      hbGrid[i].getChildren().addAll(bxGrid[i],lbGrid[i]);

      data.add(hbGrid[i]);
    }     

  Button btDeleteGrid = new Button(dialogs[42]);
  btDeleteGrid.setStyle(dialogs[0]);
  btDeleteGrid.setOnAction(new EventHandler<ActionEvent>() {
  @Override 
      public void handle(ActionEvent e) {
          if(gridList.containsKey(editingGrid)) {
 /*
              Alert alert = new Alert(AlertType.CONFIRMATION);
              alert.setTitle(dialogs[43]);
              alert.setHeaderText(dialogs[42]);
              alert.setContentText(dialogs[44]+((rede) gridList.get(editingGrid)).name);
              Optional<ButtonType> result = alert.showAndWait();
              if (result.get() == ButtonType.OK){
                  gridList.remove(editingGrid);
                  ((Label) ((HBox) ((ListView) listOfGrids.getChildren().get(0)).getItems().get(editingGrid)).getChildren().get(1)).setText("Grid");
                  desenhaCubo();                           
              }    
   
              */
          }
      }
  });

    listGridsBox.getChildren().addAll(listGrids,new TextArea(),btDeleteGrid);
    return listGridsBox;
} 

private HBox createLineOfGrid(int value) {
       int valor = value;
       CheckBox bxGrid = new CheckBox();
       bxGrid.setOnAction(new EventHandler<ActionEvent>() {
       @Override 
       public void handle(ActionEvent e) {
           if(bxGrid.isSelected()) {
               editingGrid = valor;          
               ((ListView) listOfGrids.getChildren().get(0)).getSelectionModel().select(valor);
           }

           ((TextArea) listOfGrids.getChildren().get(1)).setText(((rede) gridList.get(editingGrid)).toString());
           desenhaCubo();
       }   
   });  
       Label lbGrid = new Label(dialogs[40]);
       HBox hbGrid = new HBox();
       hbGrid.getChildren().addAll(bxGrid,lbGrid);
       return hbGrid;
}       

public static void main(String[] args) {
    launch(args);
}

private void calculaAlfa(int gridNumber[]) {
    // GridNumber[0] = Source GridNumber[1] = Target
    
	double alfaX[][] = new double[((rede) gridList.get(gridNumber[1])).lista.size()][((rede) gridList.get(gridNumber[0])).lista.size()];
	double alfaY[][] = new double[((rede) gridList.get(gridNumber[1])).lista.size()][((rede) gridList.get(gridNumber[0])).lista.size()];
	double alfaZ[][] = new double[((rede) gridList.get(gridNumber[1])).lista.size()][((rede) gridList.get(gridNumber[0])).lista.size()];
	double dGX[] = new double[((rede) gridList.get(gridNumber[1])).lista.size()];
	double dGY[] = new double[((rede) gridList.get(gridNumber[1])).lista.size()];
	double dGZ[] = new double[((rede) gridList.get(gridNumber[1])).lista.size()];
		
	double somaX, somaY, somaZ;
        celula cel = (celula) ((rede) gridList.get(gridNumber[0])).devolveCelula(0);
        double vol = calculateVolume(cel.lado);
		
	  for( int i=0; i< ((rede) gridList.get(gridNumber[1])).lista.size(); i++ ) 						// A rede da superficie 
		{
		for( int j=0; j < ((rede) gridList.get(gridNumber[0])).lista.size(); j++ ) 
			{
			celula cels = new celula();
			celula cels1 = (celula) ((rede) gridList.get(gridNumber[0])).devolveCelula(j); 							// rede 3D
			celula cels2 = (celula) ((rede) gridList.get(gridNumber[1])).devolveCelula(i); 							// rede 2D Como é calcular a distancia é indifernte lista_2D ou lista2D proposta
			cels.p = calculateDistance(cels1.p,cels2.p);
			double dist = Math.pow(Math.sqrt(Math.pow(cels.p.x,2)+Math.pow(cels.p.y,2)+Math.pow(cels.p.z,2)),3);
			alfaX[i][j] = -listaConstantes.g_universal*vol*cels.p.x/dist;
			alfaY[i][j] = -listaConstantes.g_universal*vol*cels.p.y/dist;
			alfaZ[i][j] = -listaConstantes.g_universal*vol*cels.p.z/dist;
			}
		}
		
	  for( int i=0; i< ((rede) gridList.get(gridNumber[1])).lista.size(); i++ )						// Soma os alfas para cada nó da superficie 
		{
		somaX=0; somaY=0; somaZ=0;
		for( int j=0; j < ((rede) gridList.get(gridNumber[0])).lista.size(); j++ ) 
			{
			celula cels1 = (celula) ((rede) gridList.get(gridNumber[0])).devolveCelula(j); 							// rede 3D
                        somaX=somaX+cels1.V1*alfaX[i][j]; 						// dg calculado para cada V
                        somaY=somaY+cels1.V1*alfaY[i][j];
                        somaZ=somaZ+cels1.V1*alfaZ[i][j];
                        }
		dGX[i]=somaX; dGY[i]=somaY; dGZ[i]=somaZ;
		celula cell = new celula();
		cell = (celula) ((rede) gridList.get(gridNumber[1])).devolveCelula(i); 
        	cell.V1 = -listaConstantes.g_refz-dGZ[i];												// Calcula sempre o valor de G na componente de Z

		((rede) gridList.get(gridNumber[1])).addCelula(i,cell);
	}     
    }

private void chooseGrid() {
    Stage pStage = new Stage();
    VBox vbList1 = new VBox();
    VBox vbList2 = new VBox();
    HBox menu = new HBox();
    ComboBox listSource = new ComboBox();
    ComboBox listTarget = new ComboBox();
    
    int j=0;
    for(int i = 0; i < 5; i++ ) {
            String t = ((Label) ((HBox) ((ListView) listOfGrids.getChildren().get(0)).getItems().get(i)).getChildren().get(1)).getText();
            listSource.getItems().add(j, t+": "+i);
            listTarget.getItems().add(j, t+": "+i);
            j++;
    }
    
    listSource.setPrefSize(200, 14);
    listTarget.setPrefSize(200, 14);
    listSource.getSelectionModel().selectFirst();
    listTarget.getSelectionModel().selectFirst();


    vbList1.getChildren().addAll(new Label(dialogs[11]),listSource);
    vbList2.getChildren().addAll(new Label(dialogs[12]),listTarget);
    menu.getChildren().addAll(vbList1,vbList2);


    Button btCalculate = new Button(dialogs[13]);
    btCalculate.setStyle(dialogs[0]);
    btCalculate.setOnAction(new EventHandler<ActionEvent>() {
    @Override 
        public void handle(ActionEvent e) {
            int[] returnValue = {((ComboBox) vbList1.getChildren().get(1)).getSelectionModel().getSelectedIndex(),
                        ((ComboBox) vbList2.getChildren().get(1)).getSelectionModel().getSelectedIndex()};
            calculaAlfa(returnValue);
            pStage.close();            
        }
    });
    
    VBox sceneBox = new VBox();
    sceneBox.getChildren().addAll(menu,btCalculate);
    pStage.setScene(new Scene(sceneBox, 400, 250));
    pStage.show();        
}



private void selectGrid() {
    Stage pStage = new Stage();
    pStage.setTitle("Choose Grid");
    HBox menu = new HBox();
    ListView listGrids = new ListView();
    
    int j=0;
    for(int i = 0; i < 5; i++ ) {
        String t = ((Label) ((HBox) ((ListView) listOfGrids.getChildren().get(0)).getItems().get(i)).getChildren().get(1)).getText();
        listGrids.getItems().add(j, ""+i+": "+t);
        listGrids.setOnMouseClicked(new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent mouseEvent) {            
                    if(mouseEvent.getClickCount() == 2) {
                        editingGrid = listGrids.getSelectionModel().getSelectedIndex();                    
                        pStage.close(); 
                    }
                }
            });
        j++;
    }
    
    listGrids.getSelectionModel().select(editingGrid);

    menu.getChildren().addAll(listGrids);

    
    VBox sceneBox = new VBox();
    sceneBox.getChildren().addAll(menu);
    pStage.setScene(new Scene(sceneBox, 220, 120));
    pStage.showAndWait();
    
}

/*  
  public void emiteDensidades() {										// Envia para a grelha 3D novas propostas de d
	double ale01 = Math.random();
	
		for( int j=0; j < lista3D.size(); j++ ) 
			{
			cels = new celula();
			cels = (celula)lista3D.get(j); 								// rede 3D
			cels.d_proposto = cels.d + coefRo*(ale01-.5);				// envia o valor do d (aleatorio) proposto para a grelha
			lista3D.put(j,cels);
			}
	}

  public void calculaSomaGLido() {
	
	somaGLido=0;  
	for( int i=0; i< celulas2DX*celulas2DY; i++ ) 						// A rede da superficie 
		{
		cels2 = (celula)lista2D.get(i); 								// rede 2D Como é calcular a distancia é indifernte lista_2D ou lista2D proposta
		somaGLido += cels2.d;	
		} 
}

  public void calculaEpsilon() {
	double diferencaG=0;
		
	for( int i=0; i< celulas2DX*celulas2DY; i++ ) 						// A rede da superficie 
		{
		cels2 = (celula)lista2D.get(i); 								// rede 2D Como é calcular a distancia é indifernte lista_2D ou lista2D proposta
		diferencaG += Math.abs(cels2.d-cels2.d_proposto);	
		} 
	epsilonCalculado = diferencaG/somaGLido;
		
	}
*/

}

/*
// ******************************************
// EXEMPLO DE IR BUSCAR OS ELEMENTOS DA LISTA
// Como verificar os elementos da cena

                    for(int yy = 3; yy < +cam.getChildren().size(); yy++ )
                    {
                        Object obj = cam.getChildren().get(yy);
                        if( !obj.getClass().getSimpleName().equals("Line") )
                        {
                            celula cel = (celula)cam.getChildren().get(yy).getUserData();
                        }
                    }
*/