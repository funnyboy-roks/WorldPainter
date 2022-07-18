package com.funnyboyroks;

public class MinMax {

    public long min = Long.MAX_VALUE;
    public long max = Long.MIN_VALUE;

    public MinMax(long min, long max) {
        this.min = min;
        this.max = max;
    }

    public MinMax(long... nums) {
        this.add(nums);
    }

    public MinMax(MinMax... nums) {
        this.add(nums);
    }

    public MinMax() {
    }

    public void add(long... nums) {
        for (long num : nums) {
            if (num < this.min) this.min = num;
            if (num > this.max) this.max = num;
        }
    }

    public MinMax constrain(long min, long max) {
        return new MinMax(Math.max(min, this.min), Math.min(max, this.max));
    }

    public void add(MinMax... nums) {
        for (MinMax num : nums) {
            if (num.min < this.min) this.min = num.min;
            if (num.min > this.max) this.max = num.min;
            if (num.max < this.min) this.min = num.max;
            if (num.max > this.max) this.max = num.max;
        }
    }

}
