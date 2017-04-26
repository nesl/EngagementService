package ucla.nesl.engagementservice.emascheduler.task;

import java.util.ArrayList;

/**
 * Created by timestring on 4/24/17.
 *
 * EMA-based task. It presents a questionnaire to participants.
 */
public class EMASurvey implements ITask {
    //TODO: fill in the implementation

    private ArrayList<String> questionList = new ArrayList<String>();

    public void addQuesion(String quesDescription) {
        questionList.add(quesDescription);
    }
}
