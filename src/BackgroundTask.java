import javafx.concurrent.Task;

import static java.lang.System.in;

public class BackgroundTask extends Task {
    @Override
    protected Object call() throws Exception {
        return null;
    }

    public void updateProgress(double workDone, double max){
        super.updateProgress(workDone, max);


        }
    }





