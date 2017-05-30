import java.util.ArrayList;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;







public class test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		Segment segment = HanLP.newSegment();
//		System.out.println(segment.seg("长江七号中饰演小狄的是谁"));
		QueryProcess query = new QueryProcess();
		String[] questionArr = new String[] {"27套礼服的故事简介", "长城的票房", "美人鱼是什么类型的电影", "美人鱼中谁演八哥"};
		for(String que: questionArr){
				ArrayList<String> question = query.analyQuery(que);
				searchGraph graph = new searchGraph();
				ArrayList<Object> resultListFour = graph.searchAnswer(question);
				graph.printResult(resultListFour);
				System.out.println("---------------------------------------------------------------");
		}

		/*
		 * 长江七号 角色列表 角色 周小狄 演员 名字
		 * 美人鱼 角色列表 角色 八哥 演员 名字
		 * */
		}
	

}
