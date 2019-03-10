package celsiuss.wynnquestmap;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class WorkerRunnable implements Runnable{

    private Socket clientSocket = null;

    public WorkerRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            long time = System.currentTimeMillis();

            Gson gson = new Gson();
            String json = gson.toJson(WynnQuestMap.quests);

            output.write((
                    "HTTP/1.1 200 OK\n" +
                    "Content-Type: application/json\n" +
                    "Access-Control-Allow-Origin: *\n\n" +
                    json
            ).getBytes());

            output.close();
            input.close();
            System.out.println("Request processed: " + time);
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }
}
