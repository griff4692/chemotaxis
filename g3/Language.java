package chemotaxis.g3;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList; 
import java.lang.Math;
import chemotaxis.sim.SimPrinter;

public final class Language {
    
    public static class Translator {
        
        private static SimPrinter simPrinter = new SimPrinter(false);
        private static Translator t = null;

        public static Translator getInstance() {
            if(t == null) {
               t = new Translator();
            }
            return t;
        }

        private Translator() {
            ;
        }

        // for interfacing with the Agent
        // Given directed color "uRG_", Returns current state "0+2*0+1.C"
        public static String getState(String color, Integer b) {
            // retrieve box number and coordinates for this direction
            int box = colorToBoxNum.get(color);
            // System.out.println("Box: " + box);
            String coords = boxNumToCoords[box];
            String prevState = getState(b); 
           
            simPrinter.println("Previous state was: " + prevState);
            // change  coords into state string, depending on previous movement
            //     if the prevState is n/a or silent or pause, the defaults will override 
            //                             X    ±    N   [.*]  Y    ±    M   [.*] [C/R]
            char[] newState = new char[] {'0', '+', '0', '*', '0', '+', '0', '.', 'C'};
            //                             0    1    2    3    4    5    6    7    8
            
            // correct last moved axis 
            if (prevState.charAt(3) == '.') {
                newState[3] = '.';
                newState[7] = '*';
            }

            // correct axis direction
            if (coords.charAt(0) == '-')
                    newState[1] = '-';
            if (coords.charAt(2) == '-')
                newState[5] = '-';

            // if doing lateral movement, you continually repeat 
            if (coords.charAt(1) == '0' || coords.charAt(3) == '0') {
                newState[8] = 'R';
                if (coords.charAt(1) == '0') {
                    newState[3] = '.';
                    newState[7] = '*';
                }
                else {
                    newState[7] = '.';
                    newState[3] = '*';
                }
            }

            // correct max axis movement
            newState[2] = coords.charAt(1);
            newState[6] = coords.charAt(3);

            return String.valueOf(newState);
        }

        public static String getState(Integer b) {
            return byteToState[b+128];
        }

        public static Byte getByte(char[] s) {
            Integer i = stateToByte.get(String.valueOf(s));
            if (i == null) 
                simPrinter.println("Invalid key in getByte: " + String.valueOf(s) + " " + stateToByte.get(String.valueOf(s)));
            return i.byteValue();
        }

        public Boolean validByte(char[] s) {
            return stateToByte.containsKey(String.valueOf(s));
        }

        // for interfacing with the Controller
        // Given Angle b/w agent and target, returns color + direction 
        public static String getColor(Double angle) {
            // get intercepted box # from angle 
            int box = findClosestAngle(angle);

            // return directed color, like "d_GB"
            return boxNumToColor[box];
        }

        // change if remapping color + direction to a new cell 
        private final static Map<String, Integer> colorToBoxNum =  Map.ofEntries(
            Map.entry("d__B",  0),
            Map.entry("lR__",  1),
            Map.entry("d_GB",  2 ),
            Map.entry("lRG_",  3),
            Map.entry("dR__",  4),
            Map.entry("dRG_",  5),
            Map.entry("d_G_",  6),
            Map.entry("__G_",  7),
            Map.entry("l_G_",  8),
            Map.entry("l_GB",  9),
            Map.entry("l__B", 10),
            Map.entry("_R__", 11),
            Map.entry("___B", 12),
            Map.entry("uR__", 13),
            Map.entry("uRG_", 14),
            Map.entry("u_G_", 15),
            Map.entry("_RG_", 16),
            Map.entry("r_G_", 17),
            Map.entry("r_GB", 18),
            Map.entry("r__B", 19),
            Map.entry("u_GB", 20),
            Map.entry("rRG_", 21),
            Map.entry("u__B", 22),
            Map.entry("rR__", 23)
        );

        // public static String getCoords

        // never change 
        private final static String[] boxNumToCoords = new String[] {
                            "-1+3",         "+1+3", 
                            "-1+2",         "+1+2", 
            "-3+1", "-2+1", "-1+1", "+0+1", "+1+1", "+2+1", "+3+1",
                            "-1+0",         "+1+0",
            "-3-1", "-2-1", "-1-1", "+0-1", "+1-1", "+2-1", "+3-1",
                            "-1-2",         "+1-2",
                            "-1-3",         "+1-3"
        };

        // never change 
        private static Integer findClosestAngle(double angle) {
            if (0.0 <= angle && angle < 180.0) {
                if (angle < 90.0) {
                    if (angle < 45) {
                        if (angle < 18.43) return 12;
                        else if (angle < 26.57) return 10;
                        else return 9; 
                    }
                    else {
                        if (angle < 63.43) return 8;
                        else if (angle < 71.57) return 3;
                        else return 1;
                    }
                }
                else {
                    if (angle < 135) {
                        if (angle < 108.43) return 7;
                        else if (angle < 116.57) return 0;
                        else return 2;
                    }
                    else {
                        if (angle < 153.43) return 6;
                        else if (angle < 161.57) return 5;
                        else return 4;
                    }
                }
            }
            else {
                if (angle < 270.0) {
                    if (angle < 225) {
                        if (angle < 198.83) return 11;
                        else if (angle < 206.57) return 13;
                        else return 14;
                    }
                    else {
                        if (angle < 243.43) return 15;
                        else if (angle < 251.57) return 20;
                        else return 22;
                    }
                }
                else {
                    if (angle < 315) {
                        if (angle < 288.43) return 16;
                        else if (angle < 296.57) return 23;
                        else return 21;
                    }
                    else {
                        if (angle < 333.43) return 17;
                        else if (angle < 341.57) return 18;
                        else return 19;
                    }
                }
            }
        }

        private static Boolean isPerfectAngle(double angle) {
            return (angle == 0.0  
                    || angle == 18.43 || angle == 26.57 
                    || angle == 45.0 || angle == 63.43 || angle == 71.57 
                    || angle == 90.0  
                    || angle == 108.43 || angle == 116.57 
                    || angle == 135 || angle == 153.43 || angle == 161.57 
                    || angle == 180.0
                    || angle == 198.83 || angle == 206.57 
                    || angle == 225 || angle == 243.43 || angle == 251.57 
                    || angle == 270.0
                    || angle == 288.43 || angle == 296.57 
                    || angle == 315 || angle == 333.43 || angle == 341.57);
        }

        // change if remapping color + direction to a new cell 
        private final static String[] boxNumToColor = new String[] {
            "d__B", // 0
            "lR__", // 1
            "d_GB", // 2 
            "lRG_", // 3
            "dR__", // 4
            "dRG_", // 5
            "d_G_", // 6
            "__G_", // 7
            "l_G_", // 8
            "l_GB", // 9
            "l__B", // 10
            "_R__", // 11
            "___B", // 12
            "uR__", // 13
            "uRG_", // 14
            "u_G_", // 15
            "_RG_", // 16
            "r_G_", // 17
            "r_GB", // 18
            "r__B", // 19
            "u_GB", // 20
            "rRG_", // 21
            "u__B", // 22
            "rR__"  // 23
        };

        // do not change unless the translation is changing
        private final static Map<String, Integer> stateToByte =  Map.ofEntries(
            Map.entry("silent", -128),
            Map.entry("pause", -127),
            Map.entry("0+1*0+0.R", -126),
            Map.entry("0-1*0+0.R", -125),
            Map.entry("0+0.0+1*R", -124),
            Map.entry("0+0.0-1*R", -123),
            Map.entry("0+1*0+1.C", -122),
            Map.entry("0+1.0+1*C", -121),
            Map.entry("1+1*0+1.R", -120),
            Map.entry("0+1.1+1*R", -119),
            Map.entry("0+1*0-1.C", -118),
            Map.entry("0+1.0-1*C", -117),
            Map.entry("1+1*0-1.R", -116),
            Map.entry("0+1.1-1*R", -115),
            Map.entry("0-1*0+1.C", -114),
            Map.entry("0-1.0+1*C", -113),
            Map.entry("1-1*0+1.R", -112),
            Map.entry("0-1.1+1*R", -111),
            Map.entry("0-1*0-1.C", -110),
            Map.entry("0-1.0-1*C", -109),
            Map.entry("1-1*0-1.R", -108),
            Map.entry("0-1.1-1*R", -107),
            Map.entry("0+1*0+2.C", -106),
            Map.entry("0+1.0+2*C", -105),
            Map.entry("1+1*0+2.C", -104),
            Map.entry("0+1.1+2*C", -103),
            Map.entry("1+1*1+2.R", -102),
            Map.entry("1+1.1+2*R", -101),
            Map.entry("0+1.2+2*R", -100),
            Map.entry("0+1*0-2.C", -99),
            Map.entry("0+1.0-2*C", -98),
            Map.entry("1+1*0-2.C", -97),
            Map.entry("0+1.1-2*C", -96),
            Map.entry("1+1*1-2.R", -95),
            Map.entry("1+1.1-2*R", -94),
            Map.entry("0+1.2-2*R", -93),
            Map.entry("0-1*0+2.C", -92),
            Map.entry("0-1.0+2*C", -91),
            Map.entry("1-1*0+2.C", -90),
            Map.entry("0-1.1+2*C", -89),
            Map.entry("1-1*1+2.R", -88),
            Map.entry("1-1.1+2*R", -87),
            Map.entry("0-1.2+2*R", -86),
            Map.entry("0-1*0-2.C", -85),
            Map.entry("0-1.0-2*C", -84),
            Map.entry("1-1*0-2.C", -83),
            Map.entry("0-1.1-2*C", -82),
            Map.entry("1-1*1-2.R", -81),
            Map.entry("1-1.1-2*R", -80),
            Map.entry("0-1.2-2*R", -79),
            Map.entry("0+2*0+1.C", -78),
            Map.entry("0+2.0+1*C", -77),
            Map.entry("1+2*0+1.C", -76),
            Map.entry("0+2.1+1*C", -75),
            Map.entry("1+2*1+1.R", -74),
            Map.entry("1+2.1+1*R", -73),
            Map.entry("2+2*0+1.R", -72),
            Map.entry("0+2*0-1.C", -71),
            Map.entry("0+2.0-1*C", -70),
            Map.entry("1+2*0-1.C", -69),
            Map.entry("0+2.1-1*C", -68),
            Map.entry("1+2*1-1.R", -67),
            Map.entry("1+2.1-1*R", -66),
            Map.entry("2+2*0-1.R", -65),
            Map.entry("0-2*0+1.C", -64),
            Map.entry("0-2.0+1*C", -63),
            Map.entry("1-2*0+1.C", -62),
            Map.entry("0-2.1+1*C", -61),
            Map.entry("1-2*1+1.R", -60),
            Map.entry("1-2.1+1*R", -59),
            Map.entry("2-2*0+1.R", -58),
            Map.entry("0-2*0-1.C", -57),
            Map.entry("0-2.0-1*C", -56),
            Map.entry("1-2*0-1.C", -55),
            Map.entry("0-2.1-1*C", -54),
            Map.entry("1-2*1-1.R", -53),
            Map.entry("1-2.1-1*R", -52),
            Map.entry("2-2*0-1.R", -51),
            Map.entry("0+1*0+3.C", -50),
            Map.entry("0+1.0+3*C", -49),
            Map.entry("1+1*0+3.C", -48),
            Map.entry("0+1.1+3*C", -47),
            Map.entry("1+1*1+3.C", -46),
            Map.entry("1+1.1+3*C", -45),
            Map.entry("0+1.2+3*C", -44),
            Map.entry("1+1*2+3.R", -43),
            Map.entry("1+1.2+3*R", -42),
            Map.entry("0+1.3+3*R", -41),
            Map.entry("0+1*0-3.C", -40),
            Map.entry("0+1.0-3*C", -39),
            Map.entry("1+1*0-3.C", -38),
            Map.entry("0+1.1-3*C", -37),
            Map.entry("1+1*1-3.C", -36),
            Map.entry("1+1.1-3*C", -35),
            Map.entry("0+1.2-3*C", -34),
            Map.entry("1+1*2-3.R", -33),
            Map.entry("1+1.2-3*R", -32),
            Map.entry("0+1.3-3*R", -31),
            Map.entry("0-1*0+3.C", -30),
            Map.entry("0-1.0+3*C", -29),
            Map.entry("1-1*0+3.C", -28),
            Map.entry("0-1.1+3*C", -27),
            Map.entry("1-1*1+3.C", -26),
            Map.entry("1-1.1+3*C", -25),
            Map.entry("0-1.2+3*C", -24),
            Map.entry("1-1*2+3.R", -23),
            Map.entry("1-1.2+3*R", -22),
            Map.entry("0-1.3+3*R", -21),
            Map.entry("0-1*0-3.C", -20),
            Map.entry("0-1.0-3*C", -19),
            Map.entry("1-1*0-3.C", -18),
            Map.entry("0-1.1-3*C", -17),
            Map.entry("1-1*1-3.C", -16),
            Map.entry("1-1.1-3*C", -15),
            Map.entry("0-1.2-3*C", -14),
            Map.entry("1-1*2-3.R", -13),
            Map.entry("1-1.2-3*R", -12),
            Map.entry("0-1.3-3*R", -11),
            Map.entry("0+3*0+1.C", -10),
            Map.entry("0+3.0+1*C", -9),
            Map.entry("0+3.1+1*C", -8),
            Map.entry("1+3*0+1.C", -7),
            Map.entry("1+3.1+1*C", -6),
            Map.entry("1+3*1+1.C", -5),
            Map.entry("2+3*0+1.C", -4),
            Map.entry("2+3.1+1*R", -3),
            Map.entry("2+3*1+1.R", -2),
            Map.entry("3+3*0+1.R", -1),
            Map.entry("0+0*0+0.R", 0),
            Map.entry("0+3*0-1.C", 1),
            Map.entry("0+3.0-1*C", 2),
            Map.entry("0+3.1-1*C", 3),
            Map.entry("1+3*0-1.C", 4),
            Map.entry("1+3.1-1*C", 5),
            Map.entry("1+3*1-1.C", 6),
            Map.entry("2+3*0-1.C", 7),
            Map.entry("2+3.1-1*R", 8),
            Map.entry("2+3*1-1.R", 9),
            Map.entry("3+3*0-1.R", 10),
            Map.entry("0-3*0+1.C", 11),
            Map.entry("0-3.0+1*C", 12),
            Map.entry("0-3.1+1*C", 13),
            Map.entry("1-3*0+1.C", 14),
            Map.entry("1-3.1+1*C", 15),
            Map.entry("1-3*1+1.C", 16),
            Map.entry("2-3*0+1.C", 17),
            Map.entry("2-3.1+1*R", 18),
            Map.entry("2-3*1+1.R", 19),
            Map.entry("3-3*0+1.R", 20),
            Map.entry("0-3*0-1.C", 21),
            Map.entry("0-3.0-1*C", 22),
            Map.entry("0-3.1-1*C", 23),
            Map.entry("1-3*0-1.C", 24),
            Map.entry("1-3.1-1*C", 25),
            Map.entry("1-3*1-1.C", 26),
            Map.entry("2-3*0-1.C", 27),
            Map.entry("2-3.1-1*R", 28),
            Map.entry("2-3*1-1.R", 29),
            Map.entry("3-3*0-1.R", 30),
            Map.entry("++BUW", 31),
            Map.entry("++BDW", 32),
            Map.entry("++BLW", 33),
            Map.entry("++BRW", 34),
            Map.entry("+-BUW", 35),
            Map.entry("+-BDW", 36),
            Map.entry("+-BLW", 37),
            Map.entry("+-BRW", 38),
            Map.entry("-+BUW", 39),
            Map.entry("-+BDW", 40),
            Map.entry("-+BLW", 41),
            Map.entry("-+BRW", 42),
            Map.entry("--BUW", 43),
            Map.entry("--BDW", 44),
            Map.entry("--BLW", 45),
            Map.entry("--BRW", 46),
            Map.entry("++xUW", 47),
            Map.entry("++xDW", 48),
            Map.entry("++xLW", 49),
            Map.entry("++xRW", 50),
            Map.entry("+-xUW", 51),
            Map.entry("+-xDW", 52),
            Map.entry("+-xLW", 53),
            Map.entry("+-xRW", 54),
            Map.entry("-+xUW", 55),
            Map.entry("-+xDW", 56),
            Map.entry("-+xLW", 57),
            Map.entry("-+xRW", 58),
            Map.entry("--xUW", 59),
            Map.entry("--xDW", 60),
            Map.entry("--xLW", 61),
            Map.entry("--xRW", 62),
            Map.entry("++yUW", 63),
            Map.entry("++yDW", 64),
            Map.entry("++yLW", 65),
            Map.entry("++yRW", 66),
            Map.entry("+-yUW", 67),
            Map.entry("+-yDW", 68),
            Map.entry("+-yLW", 69),
            Map.entry("+-yRW", 70),
            Map.entry("-+yUW", 71),
            Map.entry("-+yDW", 72),
            Map.entry("-+yLW", 73),
            Map.entry("-+yRW", 74),
            Map.entry("--yUW", 75),
            Map.entry("--yDW", 76),
            Map.entry("--yLW", 77),
            Map.entry("--yRW", 78),
            Map.entry("+.XUW", 79),
            Map.entry("+.XDW", 80),
            Map.entry("+.XLW", 81),
            Map.entry("+.XRW", 82),
            Map.entry("-.XUW", 83),
            Map.entry("-.XDW", 84),
            Map.entry("-.XLW", 85),
            Map.entry("-.XRW", 86),
            Map.entry(".+YUW", 87),
            Map.entry(".+YDW", 88),
            Map.entry(".+YLW", 89),
            Map.entry(".+YRW", 90),
            Map.entry(".-YUW", 91),
            Map.entry(".-YDW", 92),
            Map.entry(".-YLW", 93),
            Map.entry(".-YRW", 94)
        );

        private final static String[] byteToState = new String[] {
            // state        i   byte   
            "silent", // 0 / -128
            "pause", // 1 / -127
            "0+1*0+0.R", // 2 / -126
            "0-1*0+0.R", // 3 / -125
            "0+0.0+1*R", // 4 / -124
            "0+0.0-1*R", // 5 / -123
            "0+1*0+1.C", // 6 / -122
            "0+1.0+1*C", // 7 / -121
            "1+1*0+1.R", // 8 / -120
            "0+1.1+1*R", // 9 / -119
            "0+1*0-1.C", // 10 / -118
            "0+1.0-1*C", // 11 / -117
            "1+1*0-1.R", // 12 / -116
            "0+1.1-1*R", // 13 / -115
            "0-1*0+1.C", // 14 / -114
            "0-1.0+1*C", // 15 / -113
            "1-1*0+1.R", // 16 / -112
            "0-1.1+1*R", // 17 / -111
            "0-1*0-1.C", // 18 / -110
            "0-1.0-1*C", // 19 / -109
            "1-1*0-1.R", // 20 / -108
            "0-1.1-1*R", // 21 / -107
            "0+1*0+2.C", // 22 / -106
            "0+1.0+2*C", // 23 / -105
            "1+1*0+2.C", // 24 / -104
            "0+1.1+2*C", // 25 / -103
            "1+1*1+2.R", // 26 / -102
            "1+1.1+2*R", // 27 / -101
            "0+1.2+2*R", // 28 / -100
            "0+1*0-2.C", // 29 / -99
            "0+1.0-2*C", // 30 / -98
            "1+1*0-2.C", // 31 / -97
            "0+1.1-2*C", // 32 / -96
            "1+1*1-2.R", // 33 / -95
            "1+1.1-2*R", // 34 / -94
            "0+1.2-2*R", // 35 / -93
            "0-1*0+2.C", // 36 / -92
            "0-1.0+2*C", // 37 / -91
            "1-1*0+2.C", // 38 / -90
            "0-1.1+2*C", // 39 / -89
            "1-1*1+2.R", // 40 / -88
            "1-1.1+2*R", // 41 / -87
            "0-1.2+2*R", // 42 / -86
            "0-1*0-2.C", // 43 / -85
            "0-1.0-2*C", // 44 / -84
            "1-1*0-2.C", // 45 / -83
            "0-1.1-2*C", // 46 / -82
            "1-1*1-2.R", // 47 / -81
            "1-1.1-2*R", // 48 / -80
            "0-1.2-2*R", // 49 / -79
            "0+2*0+1.C", // 50 / -78
            "0+2.0+1*C", // 51 / -77
            "1+2*0+1.C", // 52 / -76
            "0+2.1+1*C", // 53 / -75
            "1+2*1+1.R", // 54 / -74
            "1+2.1+1*R", // 55 / -73
            "2+2*0+1.R", // 56 / -72
            "0+2*0-1.C", // 57 / -71
            "0+2.0-1*C", // 58 / -70
            "1+2*0-1.C", // 59 / -69
            "0+2.1-1*C", // 60 / -68
            "1+2*1-1.R", // 61 / -67
            "1+2.1-1*R", // 62 / -66
            "2+2*0-1.R", // 63 / -65
            "0-2*0+1.C", // 64 / -64
            "0-2.0+1*C", // 65 / -63
            "1-2*0+1.C", // 66 / -62
            "0-2.1+1*C", // 67 / -61
            "1-2*1+1.R", // 68 / -60
            "1-2.1+1*R", // 69 / -59
            "2-2*0+1.R", // 70 / -58
            "0-2*0-1.C", // 71 / -57
            "0-2.0-1*C", // 72 / -56
            "1-2*0-1.C", // 73 / -55
            "0-2.1-1*C", // 74 / -54
            "1-2*1-1.R", // 75 / -53
            "1-2.1-1*R", // 76 / -52
            "2-2*0-1.R", // 77 / -51
            "0+1*0+3.C", // 78 / -50
            "0+1.0+3*C", // 79 / -49
            "1+1*0+3.C", // 80 / -48
            "0+1.1+3*C", // 81 / -47
            "1+1*1+3.C", // 82 / -46
            "1+1.1+3*C", // 83 / -45
            "0+1.2+3*C", // 84 / -44
            "1+1*2+3.R", // 85 / -43
            "1+1.2+3*R", // 86 / -42
            "0+1.3+3*R", // 87 / -41
            "0+1*0-3.C", // 88 / -40
            "0+1.0-3*C", // 89 / -39
            "1+1*0-3.C", // 90 / -38
            "0+1.1-3*C", // 91 / -37
            "1+1*1-3.C", // 92 / -36
            "1+1.1-3*C", // 93 / -35
            "0+1.2-3*C", // 94 / -34
            "1+1*2-3.R", // 95 / -33
            "1+1.2-3*R", // 96 / -32
            "0+1.3-3*R", // 97 / -31
            "0-1*0+3.C", // 98 / -30
            "0-1.0+3*C", // 99 / -29
            "1-1*0+3.C", // 100 / -28
            "0-1.1+3*C", // 101 / -27
            "1-1*1+3.C", // 102 / -26
            "1-1.1+3*C", // 103 / -25
            "0-1.2+3*C", // 104 / -24
            "1-1*2+3.R", // 105 / -23
            "1-1.2+3*R", // 106 / -22
            "0-1.3+3*R", // 107 / -21
            "0-1*0-3.C", // 108 / -20
            "0-1.0-3*C", // 109 / -19
            "1-1*0-3.C", // 110 / -18
            "0-1.1-3*C", // 111 / -17
            "1-1*1-3.C", // 112 / -16
            "1-1.1-3*C", // 113 / -15
            "0-1.2-3*C", // 114 / -14
            "1-1*2-3.R", // 115 / -13
            "1-1.2-3*R", // 116 / -12
            "0-1.3-3*R", // 117 / -11
            "0+3*0+1.C", // 118 / -10
            "0+3.0+1*C", // 119 / -9
            "0+3.1+1*C", // 120 / -8
            "1+3*0+1.C", // 121 / -7
            "1+3.1+1*C", // 122 / -6
            "1+3*1+1.C", // 123 / -5
            "2+3*0+1.C", // 124 / -4
            "2+3.1+1*R", // 125 / -3
            "2+3*1+1.R", // 126 / -2
            "3+3*0+1.R", // 127 / -1
            "0+0*0+0.R", // 128 / 0
            "0+3*0-1.C", // 129 / 1
            "0+3.0-1*C", // 130 / 2
            "0+3.1-1*C", // 131 / 3
            "1+3*0-1.C", // 132 / 4
            "1+3.1-1*C", // 133 / 5
            "1+3*1-1.C", // 134 / 6
            "2+3*0-1.C", // 135 / 7
            "2+3.1-1*R", // 136 / 8
            "2+3*1-1.R", // 137 / 9
            "3+3*0-1.R", // 138 / 10
            "0-3*0+1.C", // 139 / 11
            "0-3.0+1*C", // 140 / 12
            "0-3.1+1*C", // 141 / 13
            "1-3*0+1.C", // 142 / 14
            "1-3.1+1*C", // 143 / 15
            "1-3*1+1.C", // 144 / 16
            "2-3*0+1.C", // 145 / 17
            "2-3.1+1*R", // 146 / 18
            "2-3*1+1.R", // 147 / 19
            "3-3*0+1.R", // 148 / 20
            "0-3*0-1.C", // 149 / 21
            "0-3.0-1*C", // 150 / 22
            "0-3.1-1*C", // 151 / 23
            "1-3*0-1.C", // 152 / 24
            "1-3.1-1*C", // 153 / 25
            "1-3*1-1.C", // 154 / 26
            "2-3*0-1.C", // 155 / 27
            "2-3.1-1*R", // 156 / 28
            "2-3*1-1.R", // 157 / 29
            "3-3*0-1.R", // 158 / 30
            "++BUW", // 159 / 31
            "++BDW", // 160 / 32
            "++BLW", // 161 / 33
            "++BRW", // 162 / 34
            "+-BUW", // 163 / 35
            "+-BDW", // 164 / 36
            "+-BLW", // 165 / 37
            "+-BRW", // 166 / 38
            "-+BUW", // 167 / 39
            "-+BDW", // 168 / 40
            "-+BLW", // 169 / 41
            "-+BRW", // 170 / 42
            "--BUW", // 171 / 43
            "--BDW", // 172 / 44
            "--BLW", // 173 / 45
            "--BRW", // 174 / 46
            "++xUW", // 175 / 47
            "++xDW", // 176 / 48
            "++xLW", // 177 / 49
            "++xRW", // 178 / 50
            "+-xUW", // 179 / 51
            "+-xDW", // 180 / 52
            "+-xLW", // 181 / 53
            "+-xRW", // 182 / 54
            "-+xUW", // 183 / 55
            "-+xDW", // 184 / 56
            "-+xLW", // 185 / 57
            "-+xRW", // 186 / 58
            "--xUW", // 187 / 59
            "--xDW", // 188 / 60
            "--xLW", // 189 / 61
            "--xRW", // 190 / 62
            "++yUW", // 191 / 63
            "++yDW", // 192 / 64
            "++yLW", // 193 / 65
            "++yRW", // 194 / 66
            "+-yUW", // 195 / 67
            "+-yDW", // 196 / 68
            "+-yLW", // 197 / 69
            "+-yRW", // 198 / 70
            "-+yUW", // 199 / 71
            "-+yDW", // 200 / 72
            "-+yLW", // 201 / 73
            "-+yRW", // 202 / 74
            "--yUW", // 203 / 75
            "--yDW", // 204 / 76
            "--yLW", // 205 / 77
            "--yRW", // 206 / 78
            "+.XUW", // 207 / 79
            "+.XDW", // 208 / 80
            "+.XLW", // 209 / 81
            "+.XRW", // 210 / 82
            "-.XUW", // 211 / 83
            "-.XDW", // 212 / 84
            "-.XLW", // 213 / 85
            "-.XRW", // 214 / 86
            ".+YUW", // 215 / 87
            ".+YDW", // 216 / 88
            ".+YLW", // 217 / 89
            ".+YRW", // 218 / 90
            ".-YUW", // 219 / 91
            ".-YDW", // 220 / 92
            ".-YLW", // 221 / 93
            ".-YRW"  // 222 / 94
        };
    }
}