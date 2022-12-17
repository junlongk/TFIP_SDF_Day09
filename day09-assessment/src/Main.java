public class Main {

    public static void main(String[] args) throws Exception {
        Integer port = 3000;
        String directory = "./target";

        for(int i=0; i<args.length; i+=2) {
            String key = args[i];
            String value = args[i+1];
    
            switch (key) {
                case "--port": 
                    port = Integer.parseInt(value); 
                    break;
                case "--docRoot": 
                    directory = value; 
                    break;
                default:
            }
        }

        System.out.printf("========\nServer will run on port: %d\n", port);
        System.out.printf("Files directory: %s\n========\n", directory);

        HttpServer.startServer(port, directory);
    }
    
}