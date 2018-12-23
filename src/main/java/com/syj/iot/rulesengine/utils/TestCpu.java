package com.syj.iot.rulesengine.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class TestCpu{
	
    private static final Logger logger = LoggerFactory.getLogger(TestCpu.class);
 
    @Scheduled(fixedRate = 1 * 60 * 1000)
    public void testCpu(){
    	int cpuCount = Runtime.getRuntime().availableProcessors();
    	cpuCount = cpuCount>0?cpuCount:2;
	    try {
	    	boolean flag=getFlag(22,24) ||getFlag(0,8) ;
	    	if(flag)
    		{
	    		while(cpuCount-->0) {
	    			new Thread(()->{
	    				int totalTime = 50000;
	    				long startTime = System.currentTimeMillis();
	    				while(true)
	    	    		{
	    	    		    doCal();
	    	    		    long left = totalTime - (System.currentTimeMillis() - startTime);
	    		    	    if(left <=0){
	    		    	    	break;    	
	    		    	    }
	    		    	    try {
								Thread.sleep(0,500);
							} catch (InterruptedException e) {
								logger.error("Exce=",e);
							}
	    		        }
	    			}).start();
	    		}
	    	}
        }catch(Exception e){
        	logger.error("##22222=",e);
        }
	}
    
    /**
     * @param date yyyy-MM-dd
     * @return
     */
    public  boolean getFlag(int start,int end){
    	Calendar c = Calendar.getInstance();
    	int hour = c.get(Calendar.HOUR_OF_DAY);
    	if(hour>=start && hour<=end)
        	return true;
        return false;
    }
    
    public static void main(String[]args){
    	while(true) {
    		;
    	}
//    	System.out.println(false||false);
    	
    }
    
    
    public static void doCal() {
	 	// 规则，只能出现数字和加减乘除符号，最前和最后都是数字，即字符串能有效计算的
//		String text = "30*9+9*7*11-10+99/3+88/11+56/7+99*3+88*11+56*7+99*11+77/11+56*7 + 30*9+9*7*11-10+99/3+88/11+56/7+99*3+88*11+56*7+99*11+77/11+56*7 + 30*9+9*7*11-10+99/3+88/11+56/7+99*3+88*11+56*7+99*11+77/11+56*7";
		String text = "30*9+9*7*11-10+99/3+88/11+56/7+99*3+88*11+56*7+99*11+77/11+56*7";
//    	logger.info("====start===="+text);
		// 计算内容分割		
		List<String> numList = new ArrayList<String>();
		int splitIndex = 0;
		for(int i=0;i<text.length();i++){
			char c = text.charAt(i);
			if(c == '+'||c == '-'||c=='*'||c=='/'){
				numList.add(text.substring(splitIndex, i));
				numList.add(c+"");
				splitIndex = i+1;
			}
		}
		// 因为使用符号做判断，增加前一位和符号，所以最后一位数字不会在循环里处理
		numList.add(text.substring(splitIndex, text.length()));

//		logger.info("====分割后====");
		for(int i=0;i<numList.size();i++){
			//System.out.println(i + " -> " + numList.get(i));
		}

		// 先做乘除计算
		List<String> list = new ArrayList<String>();
		Integer temp = null; // 用于做乘除计算临时变量
		for(int i=1;i<numList.size();i+=2){ // 这里只循环运算符号
			if("+".equals(numList.get(i))||"-".equals(numList.get(i))){
				if(null != temp){ // 存在临时变量，说明前面进行过乘除计算
					list.add(temp.toString());
					temp = null;
				} else {
					list.add(numList.get(i-1));
				}
				list.add(numList.get(i)); // 把符号加进去
				if(i==numList.size()-2) { // 处理到最后时遇到直接处理
					list.add(numList.get(i+1));
				
				
        }
			}else if("*".equals(numList.get(i))){
				if(null == temp){
					temp = Integer.parseInt(numList.get(i-1)) * Integer.parseInt(numList.get(i+1));
				}else{
					temp = temp * Integer.parseInt(numList.get(i+1));
				}
				if(i==numList.size()-2) { // 处理到最后时遇到直接处理
					list.add(temp.toString());
					temp = null;
				}
			}else if("/".equals(numList.get(i))){
				if(null == temp){
					temp = Integer.parseInt(numList.get(i-1)) / Integer.parseInt(numList.get(i+1));
				}else{
					temp = temp / Integer.parseInt(numList.get(i+1));
				}
				if(i==numList.size()-2) { // 处理到最后时遇到直接处理
					list.add(temp.toString());
					temp = null;
				}
			}
		}
//		logger.info("====乘除后====");
		for(int i=0;i<list.size();i++){
			//System.out.println(i + " -> " + list.get(i));
		}

		
		// 再做加减计算
		Integer sum = Integer.parseInt(list.get(0)); // 第一位不会在循环里处理
		for(int i=1;i<list.size();i+=2){ // 这里只循环运算符号
			if("+".equals(list.get(i))){
				sum += Integer.parseInt(list.get(i+1));
			}else if("-".equals(list.get(i))){
				sum -= Integer.parseInt(list.get(i+1));
			}
		}
		// 打印结果
//		logger.info("=sum="+sum);
		
		 
	}
    
   
}
