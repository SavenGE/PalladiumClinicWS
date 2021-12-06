/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package APalladiumclinicws;

/**
 *
 * @author neptune
 */
/*Main Class to expose Web Service*/
public class PalladiumClinicWS {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JettyServer server = new JettyServer();
        server.startServer();
    }

}
