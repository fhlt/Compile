package interpreter;

public class Utils {
	/**
	 * 判定字符串是否为纯16进制
	 * @param str
	 * @return
	 */
    public static boolean isHexNumber(String str){  
        boolean flag = true;  
        for(int i=0;i<str.length();i++){  
            char cc = str.charAt(i);  
            if(cc=='0'||cc=='1'||cc=='2'||cc=='3'||cc=='4'||cc=='5'||cc=='6'||cc=='7'||cc=='8'||cc=='9'||cc=='A'||cc=='B'||cc=='C'||  
                    cc=='D'||cc=='E'||cc=='F'||cc=='a'||cc=='b'||cc=='c'||cc=='c'||cc=='d'||cc=='e'||cc=='f'){  
                flag = true;  
            }  
            else{
            	flag = false;
            	break;
            }
        }  
        return flag;  
    }  
    /**
     * 十进制整数转十六进制字符串
     * @param dec
     * @return
     */
    public static String decToHex(int dec){
    	String hex = Integer.toHexString(dec);
    	return hex;
    }
}
