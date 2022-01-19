package com.aver.superdirector.utility;

public class Enums {
//    enum Day {
//        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
//    }
//    public static void dayPrint() {
//        System.out.print("Weekdays: ");
//        for (Day d : EnumSet.range(Day.MONDAY, Day.FRIDAY))
//            System.out.print(d + " ");
//    }

//    private int[] volumeData = {1, -1, -2, -3};
//    public int[] getVolumeData() {
//        for (int volumeDatum : volumeData) {
//            if (volumeDatum > 5) break;
//        }
//        return volumeData;
//    }

    public enum CameraItemType {
        IP(0), USB(1);
        private int value = 0;

        CameraItemType(int value) {    //    必须是private，否则編譯錯誤
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    public enum OptionEnum {
        Max(0), Min(1), Default(2), Current(3);    //    列舉項目

        private int value = 0;

        OptionEnum(int value) {    //    必须是private，否则編譯錯誤
            this.value = value;
        }

        public int value() {
            return this.value;
        }
        // 另一種寫法
        //    public static OptionEnum valueOf(int value) {
//        switch (value) {
//            case 0:
//                return Max;
//            case 1:
//                return Min;
//            case 2:
//                return Default;
//            case 3:
//                return Current;
//            default:
//                return null;
//        }
//    }
    }

    public enum PresetActions {
        Restore(0), Save(1);

        private int value = 0;

        PresetActions(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    public enum PresetAxis {
        Pan(0), Tilt(1), Zoom(2), Focus(3);

        private int value = 0;

        PresetAxis(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    public enum PresetDirect {
        RightUpInNear(0), LeftDownOutFar(1);

        private int value = 0;

        PresetDirect(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    public enum PresetCmd {
        Step(0), Start(1), Stop(2);

        private int value = 0;

        PresetCmd(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    public enum DateFormat {
        yyyyddmm(0), mmddyyyy(1), ddmmyyyy(2);

        private int value = 0;

        DateFormat(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }

        public String stringValue() {
            return Integer.toString(this.value);
        }
    }

}
