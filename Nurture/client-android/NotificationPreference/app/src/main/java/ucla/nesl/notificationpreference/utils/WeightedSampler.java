package ucla.nesl.notificationpreference.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by timestring on 7/11/18.
 *
 * Sample a pool of elements based on weight
 */

public class WeightedSampler<E> {

    private ArrayList<E> elements = new ArrayList<>();
    private ArrayList<Integer> weights = new ArrayList<>();
    private int weightSum = 0;

    public WeightedSampler<E> add(int weight, E element) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }

        elements.add(element);
        weights.add(weight);
        weightSum += weight;
        return this;
    }

    @NonNull
    public E sample(int die) {
        die %= weightSum;
        for (int i = 0; i < weights.size(); i++) {
            int tw = weights.get(i);
            if (die < tw) {
                return elements.get(i);
            }
            die -= tw;
        }
        return elements.get(0);  // make compiler happy
    }
}
