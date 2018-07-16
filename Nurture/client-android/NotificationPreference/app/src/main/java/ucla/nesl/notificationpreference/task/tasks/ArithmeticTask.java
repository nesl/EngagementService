package ucla.nesl.notificationpreference.task.tasks;

import android.support.annotation.NonNull;

import java.util.Random;

import ucla.nesl.notificationpreference.task.tasks.template.MultipleChoiceTask;
import ucla.nesl.notificationpreference.utils.WeightedSampler;

/**
 * Created by timestring on 7/11/18.
 *
 * Simple calculations such as 3+5 or 28*56.
 *
 * To check if the question is answered correctly, see buttonID = seed % 3 + 1
 */

public class ArithmeticTask extends MultipleChoiceTask {

    public static final int TASK_ID = 6;

    private static WeightedSampler<Pattern> patterns;


    private static void tryBuildPatterns() {
        if (patterns == null) {
            patterns = new WeightedSampler<Pattern>()
                    .add(1, new Pattern("1+1", 1))
                    .add(1, new Pattern("1+1", 2))
                    .add(1, new Pattern("1-1", 1))
                    .add(1, new Pattern("1-1", 2))
                    .add(1, new Pattern("2+2", 2))
                    .add(1, new Pattern("2+2", 10))
                    .add(1, new Pattern("2+2", 30))
                    .add(1, new Pattern("2-2", 1))
                    .add(1, new Pattern("2-2", 10))
                    .add(1, new Pattern("3+3", 1))
                    .add(1, new Pattern("3+3", 10))
                    .add(1, new Pattern("3+3", 100))
                    .add(1, new Pattern("1*1", 1))
                    .add(1, new Pattern("1*1", 2))
                    .add(1, new Pattern("1*1", 10))
                    .add(1, new Pattern("2*1", 1))
                    .add(1, new Pattern("2*1", 10))
                    .add(1, new Pattern("2*2", 500))
                    .add(1, new Pattern("1+1+1", 1))
                    .add(1, new Pattern("1+1+1", 2))
                    .add(1, new Pattern("1+1+1", 3))
                    .add(1, new Pattern("1+1-1", 1))
                    .add(1, new Pattern("1+1-1", 2))
                    .add(1, new Pattern("1+1-1", 3))
                    .add(1, new Pattern("1-1+1", 1))
                    .add(1, new Pattern("1-1+1", 2))
                    .add(1, new Pattern("1-1+1", 3));
        }
    }


    private Pattern pattern;

    public ArithmeticTask(int notificationID, int seed) {
        super(notificationID);
        tryBuildPatterns();
        pattern = patterns.sample(seed);
        pattern.build(seed);
    }

    @Override
    public int getTypeID() {
        return TASK_ID;
    }

    public static int sampleQuestionSeedIfCreatedNow() {
        return new Random().nextInt(1000000);
    }

    public static long getCoolDownTime() {
        return 0L;
    }

    @NonNull
    @Override
    public String getPrimaryQuestionStatement() {
        return "What is the answer of " + pattern.getFormula();
    }

    @NonNull
    @Override
    protected String[] getOptions() {
        return pattern.getOptionsInString();
    }


    private static class Pattern {
        private String template;
        private int delta;
        private int minAns;
        private int maxAns;
        private int numNumbers;
        private int[] numbers;
        private int[] options;


        private Pattern(String _template, int _delta) {
            template = _template;
            delta = _delta;
            numNumbers = template.length() / 2 + 1;
            numbers = new int[numNumbers];
            options = new int[3];

            minAns = evaluateMinValue();
            maxAns = evaluateMaxValue();
        }

        private int evaluateMinValue() {
            int[] tmpNumbers = new int[numNumbers];
            tmpNumbers[0] = getMinNumber(template.charAt(0));
            for (int i = 1; i < numNumbers; i++) {
                switch (template.charAt(i * 2 - 1)) {
                    case '+':
                    case '*':
                        tmpNumbers[i] = getMinNumber(template.charAt(i * 2));
                        break;
                    case '-':
                        tmpNumbers[i] = getMaxNumber(template.charAt(i * 2));
                }
            }
            return evaluate(tmpNumbers);
        }

        private int evaluateMaxValue() {
            int[] tmpNumbers = new int[numNumbers];
            tmpNumbers[0] = getMaxNumber(template.charAt(0));
            for (int i = 1; i < numNumbers; i++) {
                switch (template.charAt(i * 2 - 1)) {
                    case '+':
                    case '*':
                        tmpNumbers[i] = getMaxNumber(template.charAt(i * 2));
                        break;
                    case '-':
                        tmpNumbers[i] = getMinNumber(template.charAt(i * 2));
                }
            }
            return evaluate(tmpNumbers);
        }

        private void build(int seed) {
            int correctIdx = seed % 3;

            Random random = new Random(seed);

            boolean searching = true;
            while (searching) {
                for (int i = 0; i < numNumbers; i++) {
                    numbers[i] = getNumber(random, template.charAt(i * 2));
                }
                int answer = evaluate(numbers);
                for (int i = 0; i < 3; i++) {
                    options[i] = answer + (i - correctIdx) * delta;
                }
                if (checkAllOptionsValid(options)) {
                    searching = false;
                }
            }
        }

        private int getNumber(Random random, char d) {
            int minVal = getMinNumber(d);
            int maxVal = getMaxNumber(d);
            return random.nextInt(maxVal - minVal + 1) + minVal;
        }

        private int getMinNumber(char d) {
            switch (d) {
                case '1':
                    return 0;
                case '2':
                    return 10;
                case '3':
                    return 100;
            }
            throw new IndexOutOfBoundsException();
        }

        private int getMaxNumber(char d) {
            switch (d) {
                case '1':
                    return 9;
                case '2':
                    return 99;
                case '3':
                    return 999;
            }
            throw new IndexOutOfBoundsException();
        }

        private int evaluate(int[] numbers) {
            int ans = numbers[0];
            for (int i = 1; i < numbers.length; i++) {
                switch (template.charAt(i * 2 - 1)) {
                    case '+':
                        ans = ans + numbers[i];
                        break;
                    case '-':
                        ans = ans - numbers[i];
                        break;
                    case '*':
                        ans = ans * numbers[i];
                        break;
                }
            }
            return ans;
        }

        private boolean checkAllOptionsValid(int[] numbers) {
            for (int n : numbers) {
                if (n < minAns || n > maxAns) {
                    return false;
                }
            }
            return true;
        }

        private String getFormula() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < template.length(); i++) {
                if (i % 2 == 0) {
                    sb.append(numbers[i / 2]);
                } else {
                    sb.append(template.charAt(i));
                }
            }
            return sb.toString();
        }

        private String[] getOptionsInString() {
            String[] strOptions = new String[3];
            for (int i = 0; i < 3; i++) {
                strOptions[i] = String.valueOf(options[i]);
            }
            return strOptions;
        }
    }
}
