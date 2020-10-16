package server;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;


/** REFERENCE TO RESIZE IMAGE    https://www.journaldev.com/615/java-resize-image
 * This class will resize all the images in a given folder
 * @author pankaj
 */

class ImageEditor {
    public static String PATH ;
    public String FILE_NAME ;
    public String FILE_PATH ;
    public int COUNT = 0;
    public int WIDTH ;
    public int HEIGHT ;

    /**
     * This function resize the image file and returns the BufferedImage object that can be saved to file system.
     */
    public static BufferedImage resizeImage(final Image image, int width, int height) {
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.Src);
        //below three lines are for RenderingHints for better image quality at cost of higher processing time
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }

    /*
     * REFERENCE TO BW CODE https://memorynotfound.com/convert-image-black-white-java/
     * */

    public static BufferedImage toBlackAndWhite(final BufferedImage image){
        BufferedImage result = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_BYTE_BINARY);

        Graphics2D graphic = result.createGraphics();
        graphic.drawImage(image, 0, 0, Color.WHITE, null);
        graphic.dispose();

        return result;
    }

    public  void editImage() throws Exception {
        final Image img = readImageRecievedFromClient();

        BufferedImage tempJPG = (BufferedImage)img;

        showImage(tempJPG);

        tempJPG = ImageEditor.resizeImage(img, WIDTH, HEIGHT);

        saveImage(tempJPG , "crop");
        showImage(tempJPG);

        tempJPG = ImageEditor.toBlackAndWhite(tempJPG);

        saveImage(tempJPG, "bw");
        showImage(tempJPG);
    }

    private  void showImage(BufferedImage image){
        ImageIcon imageIcon = new ImageIcon(image);
        JLabel jlabel = new JLabel(imageIcon);

        JPanel painel = new JPanel();
        painel.add(jlabel);

        JFrame janela = new JFrame("ALGUMA IMAGEM");

        janela.add(painel);
        janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        janela.pack();
        janela.setLocation(15 + 100*COUNT,15 + 50*COUNT);
        janela.setVisible(true);
    }

    private  Image readImageRecievedFromClient(){
        Image img = null;
        try {
            File f = new File(FILE_PATH);
            img = ImageIO.read(f);
        } catch (Exception e) {
            System.out.println("[SERVER] provavelmente O FILE_PATH não existe");
        }

        if (img == null) throw new RuntimeException("[SERVER] A imagem recebida está quebrada, isso acontece algumas vezes");

        return img;
    }

    private  void saveImage(BufferedImage image, String editionType) throws Exception {
        File newFileJPG =  new File(String.format("%s%s-%s",PATH,editionType,FILE_NAME));
        try{ImageIO.write(image, "jpg", newFileJPG);}
        catch (Exception e){
            System.out.println("[SERVER] Não foi possível ler a imagem");
            throw e;
        }
    }

    void setFilePath(String path, String fileName) {
        FILE_PATH = path + fileName;
    }
}

