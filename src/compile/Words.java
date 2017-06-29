package compile;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 单词
 */
public class Words {
	public static Map<Integer, String> key= new HashMap<Integer, String>();
	public static Map<Integer, String> boundarySign= new HashMap<Integer, String>();
	public static Map<Integer, String> operator= new HashMap<Integer, String>();
	//种别码
	public final static String KEY = "关键字";
	public final static String OPERATOR = "运算符";
	public final static String STRING_CONST = "字符串";
	public final static String INT_CONST = "整型常量";
	public final static String DOUBLE_CONST = "浮点常量";
	public final static String CHAR_CONST = "字符常量";
	public final static String IDENTIFIER = "标志符";
	public final static String BOUNDARYSIGN = "界符";
	public final static String END = "结束符";
	public final static String UNIDEF = "未知类型";
	static{
		/*
		 * 32个关键字
		 */
		key.put(0, "program");//sample
		key.put(1, "integer");//
		key.put(2, "double");
		key.put(3, "long");
		key.put(4, "char");
		key.put(5, "float");
		key.put(6, "repeat");
		key.put(7, "int");
		key.put(8, "unsigned");
		key.put(9, "to");
		key.put(10, "then");
		key.put(11, "and");
		key.put(12, "static");
		key.put(13, "switch");
		key.put(14, "case");
		key.put(15, "default");
		key.put(16, "break");
		key.put(17, "begin");//sample
		key.put(18, "const");
		key.put(19, "end");//sample
		key.put(20, "until");
		key.put(21, "extern");
		key.put(22, "return");
		key.put(23, "void");
		key.put(24, "continue");
		key.put(25, "do");
		key.put(26, "while");
		key.put(27, "if");
		key.put(28, "else");
		key.put(29, "for");
		key.put(30, "var");//sample
		key.put(31, "sizeof");
		key.put(32, "read");
		key.put(33, "write");
		/*
		 * 界符
		 */
		boundarySign.put(32, "(");
		boundarySign.put(33, ")");
		boundarySign.put(34, "{");
		boundarySign.put(35, "}");
		boundarySign.put(36, "[");
		boundarySign.put(37, "]");
		boundarySign.put(38, "'");
		boundarySign.put(39, "\"");
		boundarySign.put(40, ";");
		boundarySign.put(41, ",");
		boundarySign.put(42,":");
		boundarySign.put(59, ".");
		/*
		 * 运算符
		 */
		operator.put(43, "+");
		operator.put(44, "-");
		operator.put(45, "++");
		operator.put(46, "--");
		operator.put(47, "*");
		operator.put(48, "/");
		operator.put(49, ">");
		operator.put(50, "<");
		operator.put(51, ">=");
		operator.put(52, "<=");
		operator.put(53, "==");
		operator.put(54, "!=");
		operator.put(55, "=");
		operator.put(56, "&&");
		operator.put(57, "||");
		operator.put(58, "!");
//		operator.put(59, ".");
		operator.put(60, "?");
		operator.put(61, "|");
		operator.put(62, "&");
		operator.put(63, "%");
		operator.put(64, ":=");
		operator.put(65, "<<");
		operator.put(66, ">>");
		operator.put(67, "^");
		
	}
	
	private int id;//单词入口（即编号）
	private String word;//单词的名字
	private int length;//单词的长度
	private String type;//单词的类型
	private String attribute;//单词的种属
	private String value;//单词的值
	private int address;//内存地址
	
	private int line;// 单词所在行
	private boolean flag = true;//单词是否合法
	//构造函数
	public Words(int wordCount,String word,int line,int type){
		setId(wordCount);
		this.setWord(word);
		setValue(null);
		setLength(word.length());
		setType(null);//暂时无法得知
		attribute = UNIDEF;
		/*
		 * 字符常量
		 */
		if(type == 0){
			attribute = BOUNDARYSIGN;
		}
		if(type == 3){
			attribute = CHAR_CONST;
		}
		if(type == 4){
			attribute = STRING_CONST;
		}
		/*
		 * 关键字判断
		 */
		Iterator iter = key.entrySet().iterator();
		while(type == 6 && iter.hasNext()){
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			String val = (String) entry.getValue();
			if(val.equals(word)){
				attribute = KEY;
				break;
			}
		}
		if(attribute == UNIDEF && type == 6){
			attribute = IDENTIFIER;
		}
		
		/*
		 * 界符判定
		 */
		if(attribute == UNIDEF && type == 0){
			iter = boundarySign.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				String val = (String) entry.getValue();
				if(val.equals(word)){
					attribute = BOUNDARYSIGN;
					break;
				}
			}
		}
		/*
		 * 操作符判定
		 */
		if(attribute == UNIDEF && type == 1){
			iter = operator.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				String val = (String) entry.getValue();
				if(val.equals(word)){
					attribute = OPERATOR;
					break;
				}
			}
		}
		
		/*
		 * 整型常量
		 */
		if(attribute == UNIDEF && type == 5){
			if(isInteger(word)){
				setValue(word);
				attribute = INT_CONST;//整形常量
			}
			else{
				setValue(word);
				attribute = DOUBLE_CONST;//浮点常量
			}
		}
		this.setLine(line);
		setAddress(-1);
		flag = true;//暂时全部置为true
	}
	/**
	 * 双目运算符判定
	 * @param op
	 * @return
	 */
	public static boolean isBinaryOperator(String op){
		Boolean flag = false;
		Iterator iter = operator.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			String val = (String) entry.getValue();
			if(val.equals(op)){
				flag = true;
				break;
			}
		}
		return flag;
	}
	/**
	 * 判断单词是否为int常量
	 */
	public static boolean isInteger(String word) {
		int i = 0;
		boolean flag = false;
		
		if(word.charAt(0) == '+' || word.charAt(0) == '-'){
			i = 1;
		}
		for (; i < word.length(); i++) {
			if (Character.isDigit(word.charAt(i))) {
				continue;
			} else {
				break;
			}
		}
		if (i == word.length()) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 判断单词是否为char常量
	 */
	private static boolean isChar(String word) {
		boolean flag = false;
		int i = 0;
		char temp = word.charAt(i);
		if (temp == '\'') {
			for (i = 1; i < word.length(); i++) {
				temp = word.charAt(i);
				if (0 <= temp && temp <= 255)
					continue;
				else
					break;
			}
			if (i + 1 == word.length() && word.charAt(i) == '\'')
				flag = true;
		} else
			return flag;

		return flag;
	}
	public String getAttr() {
		return attribute;
	}
	public void setAttr(String attribute) {
		this.attribute = attribute;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getAddress() {
		return address;
	}
	public void setAddress(int address) {
		this.address = address;
	}
}
