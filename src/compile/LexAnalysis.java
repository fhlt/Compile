package compile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.plaf.synth.SynthStyle;

import org.w3c.dom.css.ElementCSSInlineStyle;
/**
 * 词法分析
 * @author Liangtao
 *
 */
public class LexAnalysis {
	private String sourcefile;
	public Map<Integer, Words> wordMap= new HashMap<Integer, Words>();//token串
	public Map<Integer, Error> errorMap= new HashMap<Integer, Error>();//错误信息
	public Map<String, Words> idMap = new HashMap<String, Words>();//符号表
	private int wordCount = 0;
	private int errCount = 0;
	private int idCount = 0;
	
	public LexAnalysis(String string){
		sourcefile = delNote(string);
		lexAnalysis(sourcefile);
	}
	private void lexAnalysis(String str){
		int i;
		String []arr = str.split("\n");
		for(i = 0;i < arr.length ;i++){
			lexAnalysisByLine(arr[i],i+1);
		}
	}
	/**
	 * 逐行进行词法分析
	 * @param str line
	 */
	private void lexAnalysisByLine(String str,int line){
		String word = "";
		Words tmpWord = null;
		Error errWord = null;
		int state = 0;//初始状态
		int index = 0;
		str+="  ";//添加两个空格
		char ch = 0,prevCh;
		while(index < str.length()){
			prevCh = ch;
			ch = str.charAt(index);
			if(isLegal(ch) == false){//如果该字符不合法
				tmpWord = new Words(errCount, ""+ch, line, -1);
				errWord = new Error(errCount, "无法识别的字符\t", line, tmpWord);
				errorMap.put(errCount++, errWord);
				state = 0;
				index++;
				continue;
			}
			switch(state){
			case 0:
				//运算符
				if(ch == '+' || ch == '-' ||
					ch == '!' || ch == '&' ||
					ch == '|' || ch == '>' ||
					ch == '<' || ch == '%' || 
					ch == '^' || ch == ':' ||
					ch == '?' || ch == '=' || 
					ch == '*' || ch == '/'){
					int flag = 0;
					if((ch == '+' || ch == '-')){
						if(wordCount >= 1){
							String attr = wordMap.get(wordCount-1).getAttr();
							if(attr.equals(Words.INT_CONST) 
									|| attr.equals(Words.DOUBLE_CONST) 
									|| attr.equals(Words.IDENTIFIER)
									|| attr.equals(Words.BOUNDARYSIGN)
									|| wordMap.get(wordCount-1).getWord().equals(":=")){
								;
								//运算符 提交后边处理
							}
							else{//正负号
								word+=ch;
								state = 1;
								flag = 1;
							}
						}
						else{
							word+=ch;
							state = 1;
							flag = 1;
						}
					}
					if(flag == 0){
						word+=ch;
						if(index+1 < str.length()){
							String opeartor = word + str.charAt(index+1);
							if(Words.isBinaryOperator(opeartor)){
								word = opeartor;
								tmpWord = new Words(wordCount, word, line, 1);//操作符
								wordMap.put(wordCount++, tmpWord);
								state = 0;
								word = "";
								index = index+2;
								continue;
							}
						}
						tmpWord = new Words(wordCount, word, line, 1);
						wordMap.put(wordCount++, tmpWord);
						state = 0;
						word = "";
						index = index+1;
						continue;
					}
				}
				//单界符
				else if(ch == '(' || ch == ')' ||
				   ch == '{' || ch == '}' ||
				   ch == '[' || ch == ']' ||
				   ch == '\''|| ch == '"' ||
				   ch == ';' || ch == ':' ||
				   ch == ',' || ch == '\\'||
				   ch == '#' || ch == '.'){
					word+=ch;
					tmpWord = new Words(wordCount, word, line, 0);//界符
					wordMap.put(wordCount++, tmpWord);
					state = 0;
					word = "";
					index++;
					//字符
					if(ch == '\''){
						state = 11;
					}
					//字符串
					if(ch == '"'){
						state = 12;
					}
					continue;
				}
				else if(isDigit(ch)){
						word+=ch;
						state = 2;
				}
				else if(isLetter(ch) || ch == '_'){
					word+=ch;
					state = 9;
				}
				break;
			case 1:
				if(prevCh == '+' || prevCh == '-'){
					if(isDigit(ch)){
						word+=ch;
						state = 2;
					}
					else{
						//加减号
					}
				}
				else if(isDigit(ch)){
					word+=ch;
					state = 2;
				}
				else{
					state = 8;
				}
				break;
			case 2:
				if(isDigit(ch)){
					word+=ch;
					state = 2;
				}
				else if(ch == 'e' || ch == 'E'){
					word+=ch;
					state = 5;
				}
				else if(ch == '.'){
					word+=ch;
					state = 3;
				}
				else{
					state = 8;
				}
				break;
			case 3:
				if(isDigit(ch)){
					word+=ch;
					state = 4;
				}
				else{
					//小数点后没有其他数字时的处理
					word+=ch;
					tmpWord = new Words(errCount, word, line, -1);
					errWord = new Error(errCount, "小数点后没有其他数字", line, tmpWord);
					errorMap.put(errCount++, errWord);
					state = 0;
					word = "";
					break;
				}
				break;
			case 4:
				if(isDigit(ch)){
					word+=ch;
					state = 4;
				}
				else if(ch == 'e' || ch == 'E'){
					word+=ch;
					state = 5;
				}
				else{
					state = 8;
				}
				break;
			case 5:
				if(ch == '+' || ch == '-'){
					word+=ch;
					state = 6;
				}
				else if(isDigit(ch)){
					word+=ch;
					state = 7;
				}
				else{
					//e/E 后不是+、-
					word+=ch;
					tmpWord = new Words(errCount, word, line, -1);
					errWord = new Error(errCount, "e/E 后不是+、- is error", line, tmpWord);
					errorMap.put(errCount++, errWord);
					state = 0;
					word = "";
					break;
				}
				break;
			case 6:
				if(isDigit(ch)){
					word+=ch;
					state = 7;
				}
				else{
					//指数+、-号后无后续数字
					word+=ch;
					tmpWord = new Words(errCount, word, line, -1);
					errWord = new Error(errCount, "指数+、-号后无后续数字 is error", line, tmpWord);
					errorMap.put(errCount++, errWord);
					state = 0;
					word = "";
					break;
				}
				break;
			case 7:
				if(isDigit(ch)){
					word+=ch;
					state = 7;
				}
				else{
					state = 8;
				}
				break;
			case 8:
				//处理可带正负号的实数
				if(word.charAt(0) == '0' && word.length()>1){
					tmpWord = new Words(errCount, word, line, -1);
					errWord = new Error(errCount, "数字不能0开头\t", line, tmpWord);
					errorMap.put(errCount++, errWord);
				}
				else{
					tmpWord = new Words(wordCount, word, line, 5);
					wordMap.put(wordCount++, tmpWord);
					if(!isInIdMap(tmpWord.getWord())){
						tmpWord = new Words(idCount++, word, line, 5);
						idMap.put(tmpWord.getWord(), tmpWord);
					}
				}
				state = 0;
				word = "";
				index--;
				continue;
			case 9:
				if(isDigit(ch) || isLetter(ch) || ch == '_' ){
					word+=ch;
					state = 9;
				}
				else {
					state = 10;
				}
				break;
			case 10:
				//处理标识符以及关键字
				tmpWord = new Words(wordCount, word, line, 6);
				wordMap.put(wordCount++, tmpWord);
				
				if(tmpWord.getAttr().equals(Words.IDENTIFIER) && !isInIdMap(tmpWord.getWord())){
					tmpWord = new Words(idCount++, word, line, 6);
					idMap.put(tmpWord.getWord(), tmpWord);
				}
				
				state = 0;
				word = "";
				index--;
				continue;
			case 11:
				if(ch != '\''){
					word+=ch;
				}
				else if(ch == '\''){
					//处理一个单界符与未知字符
					//首先判定word是否为正常字符
					if(isChar(word)){
						tmpWord = new Words(wordCount, word, line, 3);
						wordMap.put(wordCount++, tmpWord);
					}
					else{
						tmpWord = new Words(errCount, word, line, -1);
						errWord = new Error(errCount, "无法识别的字符\t", line, tmpWord);
						errorMap.put(errCount++, errWord);
					}
					tmpWord = new Words(wordCount, '\''+"", line, 0);
					wordMap.put(wordCount++, tmpWord);
					word="";
					state = 0;
				}
				else if(ch == ';'){
					//错误处理
					word="";
					state = 0;
					continue;
				}
				break;
			case 12:
				if(ch != '"'){
					word+=ch;
				}
				else if(ch == '"'){
					//字符串常量
					tmpWord = new Words(wordCount, word, line, 4);//字符串常量
					wordMap.put(wordCount++, tmpWord);
					//右双引号
					tmpWord = new Words(wordCount, '"'+"", line, 0);
					wordMap.put(wordCount++, tmpWord);
					word="";
					state = 0;
				}
				else if(ch == ';'){
					//错误处理
					word="";
					state = 0;
					continue;
				}
				break;
				default:
					break;
			}
			index++;
		}
	}
	/**
	 * 删除注释
	 * @param text
	 * @return
	 */
	public String delNote(String text) {
		// TODO Auto-generated method stub
		int index = 0;
		StringBuffer buffer = new StringBuffer("");
	    int state = 0;//初始状态
	    char  prevCh = 0, ch;//ch为当前读取的字符，prevCh为上一个字符
	    while(index < text.length()) {
	    	ch = text.charAt(index++);
	        switch (state) {
	        case 0:
	            if (ch == '/') {
	                state = 1;
	            }
	            else if (ch == '\'' || ch == '"') {
	            	buffer.append(ch);
	                prevCh = ch;
	                state = 6;
	            }
	            else {
	            	buffer.append(ch);
	            }
	            break;
	        case 1:
	            if (ch == '/') {
	                state = 2;
	            }
	            else if (ch == '*') {
	                state = 4;
	            }
	            else {
	            	buffer.append('/');
	            	buffer.append(ch);
	                state = 0;
	            }
	            break;
	        case 2:
	            if (ch == '\\') {
	                state = 3;
	            }
	            else if (ch == '\n') {
	            	buffer.append(ch);
	                state = 0;
	            }
	            break;
	        case 3:
	            if (ch != '\\') {
	                state = 2;
	            }
	            break;
	        case 4:
	            if (ch == '*') {
	                state = 5;
	            }
	            break;
	        case 5:
	            if (ch == '/') {
	                state = 0;
	            }
	            else if (ch != '*') {
	                state = 4;
	            }
	            break;
	        case 6:
	            if (ch == '\\') {
	            	buffer.append(ch);
	                state = 7;
	            }
	            else if (ch == prevCh) {
	            	buffer.append(ch);
	                state = 0;
	            }
	            else {
	            	buffer.append(ch);
	            }
	            break;
	        case 7:
	        	buffer.append(ch);
	            state = 6;
	            break;
	        default:
	        	buffer.append(ch);
	            break;
	        }
	    }
	    return buffer.toString();
	}
	/**
	 * 判定一串字符是否为单字符或转义字符
	 * @param string
	 * @return
	 */
	private Boolean isChar(String string){
		if(string.length() == 1 && isLetter(string.charAt(0))){
			return true;
		}
		else if(string.startsWith("\\") && string.length() == 2){
			return true;
		}
		return false;
	}
	/**
	 * 判断是否为数字
	 * @param ch
	 * @return
	 */
	private Boolean isDigit(char ch){
		if(ch >= '0' && ch <= '9')
			return true;
		else
			return false;
	}
	/**
	 * 判断是否为字母
	 * @param ch
	 * @return
	 */
	private Boolean isLetter(char ch){
		if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
			return true;
		else
			return false;
	}
	/**
	 * 判断字符是否合法
	 * @param ch
	 * @return
	 */
	private Boolean isLegal(char ch){
		if(isDigit(ch) || isLetter(ch) || ch == '_' || ch == ' ' || ch == '#' || ch == '\r' || ch =='\t'){
			return true;
		}
		else if(ch == '+' || ch == '-' ||
				ch == '!' || ch == '&' ||
				ch == '|' || ch == '>' ||
				ch == '<' || ch == '.' ||
				ch == '%' || ch == '^' ||
				ch == ':' || ch == '?' ||
				ch == '=' || ch == '*' ||
				ch == '/'){
			return true;
		}
		else if(ch == '(' || ch == ')' ||
				   ch == '{' || ch == '}' ||
				   ch == '[' || ch == ']' ||
				   ch == '\''|| ch == '"' ||
				   ch == ';' || ch == ':' ||
				   ch == ',' || ch == '\\'){
			return true;
		}
		else{
			return false;
		}
	}
	/**
	 * getErrCount()
	 * @return
	 */
	public int getErrCount(){
		return errCount;
	}
	/**
	 * getIdCount()
	 * @return
	 */
	public int getIdCount(){
		return idCount;
	}
	/**
	 * getWordCount()
	 * @return
	 */
	public int getWordCount(){
		return wordCount;
	}
	/**
	 * str是否在符号表中
	 * @param str
	 * @return
	 */
	private Boolean isInIdMap(String str){
		if(idMap.containsKey(str))
			return true;
		return false;
	}
	public void checkAndFillVal(Words token,String value){
		idMap.get(token.getWord()).setValue(value);
	}
	/**
	 * 查填标识符类型
	 * @param token
	 */
	public void checkAndFillType(Words token,String type){
		idMap.get(token.getWord()).setType(type);
	}
	/**
	 * 获取最新符号表信息
	 * @return
	 */
	public String getIdMapInfo(){
		String string = "入口\t" + "token\t" + "类型\t" + "行\t" + "种属\t" + "值\t" + "内存地址\t\n";
		StringBuilder builder = new StringBuilder(string);
		Iterator iter = idMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
//			Object key = entry.getKey();
			Words val = (Words) entry.getValue();
			string = val.getId() + "\t" + val.getWord() + "\t" + val.getType() + "\t" + val.getLine() + "\t"
					+ val.getAttr() + "\t" + val.getValue() + "\t" + val.getAddress() + "\t\n";
			builder.append(string);
		}
		return builder.toString();
	}
	/**
	 * 根据单词值（变量名）查符号表。
	 * 若找到，则返回变量在符号表中的入口；若符号表中无该变量的记录，则返回-1
	 * @param word
	 * @return
	 */
	public int sym_entry(String word){
//		if(idMap.containsKey(word))
		if(idMap.get(word).getType() != null)
			return idMap.get(word).getId();
		return -1;
	}
	
}
