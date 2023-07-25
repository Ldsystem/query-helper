package com.forestar.queryhelper.constant;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/24
 * @Description
 */
public enum Fuzzy {
    None,
    Original,
    LEFT{
        @Override
        public String fuzzy(String in) {
            return "%" + in;
        }},
    RIGHT{
        @Override
        public String fuzzy(String in) {
            return in + "%";
        }
    },
    BOTH {
        @Override
        public String fuzzy(String in) {
            return "%" + in + "%";
        }
    };

    public String fuzzy(String in) {
        return in;
    }

}
