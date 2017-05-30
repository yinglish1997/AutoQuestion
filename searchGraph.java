

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import learnNeo.Movie.MyRelationshipTypes;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;


import Graph.MovieGraph.Labels;

public class searchGraph {
	//莱文斯坦距离
	/**
	 * 通过查询图返回答案
	 * mapNode 查询中表示结点的中文与数据库中结点名称（英文）的映射
	 * mapProperty 查询中表示属性的中文与数据库中属性的映射
	 */
	public GraphDatabaseService graphDB;
	Map<String, Labels> mapNode = new HashMap<String, Labels>();
	Map<String, String> mapProperty = new HashMap<String, String>();
	
	public searchGraph(){
		//完善映射
		this.graphDB = null;
		this.mapNode.put("演员", Labels.ACTOR);
		this.mapNode.put("出品公司", Labels.COMPANY);
		this.mapNode.put("信息", Labels.CONTENCE);
		this.mapNode.put("导演", Labels.DIRECTOR);
		this.mapNode.put("制作", Labels.MANUFACTURE);
		this.mapNode.put("角色", Labels.ROLE);
		this.mapNode.put("角色列表", Labels.ROLELIST);
		this.mapNode.put("编剧", Labels.SCRIPTWRITER);
		this.mapNode.put("主题", Labels.THEME);
		
		this.mapProperty.put("名字", "name");
		this.mapProperty.put("评分", "score");
		this.mapProperty.put("上映", "release_time");
		this.mapProperty.put("票房", "boxOffice");
		this.mapProperty.put("地区", "area");
		this.mapProperty.put("关键词", "keywords");
		this.mapProperty.put("风格", "style");
		this.mapProperty.put("题材", "theme");
		this.mapProperty.put("相关电影", "similarMovie");
		this.mapProperty.put("获奖", "prize");
		this.mapProperty.put("剧情", "story");
		this.mapProperty.put("影评", "comment");
		this.mapProperty.put("代表作品", "represent");//注意，对于公司结点，它对应的属性应该是pastRepresent
		this.mapProperty.put("未来作品", "futureRepresent");
		this.mapProperty.put("过去作品", "pastRepresent");
		this.mapProperty.put("受欢迎程度", "popular");
		this.mapProperty.put("国籍", "nation");
		this.mapProperty.put("出生日期", "birthday");
		
		this.mapProperty.put("出品公司列表", "companyList");
		this.mapProperty.put("导演列表", "director");
		this.mapProperty.put("编剧列表", "scriptwriters");
		this.mapProperty.put("简介", "introduction");
	}
	
	
	public ArrayList<Node>  findNodeByLabel(Labels label){ 
		//通过标签查找结点，返回结点链表
		ArrayList<Node> nodeList = new ArrayList<Node>();
		Node node= null;
		try(Transaction tx = this.graphDB.beginTx()){
			 	ResourceIterator<Node> iterator = this.graphDB.findNodes(label);			
			 	while(iterator.hasNext()){
			 		node = iterator.next();
			 		nodeList.add(node);
			 	}
			 	if(node == null){
			 		System.out.println("wrong to find the node" + label.name());
			 	}
			 	tx.success();
		}
		return nodeList;
	}
	
	public ArrayList<Object>  getProperty(ArrayList<Node> nextNode, ArrayList<String> wordList, int i){
		ArrayList<Object> result = new ArrayList<Object>();
		String property = this.mapProperty.get(wordList.get(i));//把属性词转换为对应的英文单词
		//是不是单个结点，如三个一级结点：主题，制作，内容
		if(nextNode.size() == 1){
				//System.out.println(i + " node is a singnal node");
				Node singleNode = nextNode.get(0);
				result.add(singleNode.getProperty(property));
			}
			else{
			//多结点
				//System.out.println(i + " node is a double nodes ");
				for(int j = 0; j < nextNode.size(); j ++){
				//一个标签为多个结点共有，针对公司、编剧、导演、角色、演员
						Node node = nextNode.get(j);
						//由于之前给公司、导演的名字属性设置了与其它结点不一样的英文名，因此这里要判断
						if(node.hasProperty("cname")){						
						result.add("company" + j + ": " + nextNode.get(j).getProperty("cname"));
						}else{
							if(node.hasProperty("dname")){
										result.add("director" + j + ": " + nextNode.get(j).getProperty("dname"));
							}
							else{
										result.add("the " + j + "th one is : " + nextNode.get(j).getProperty("name"));
										}
						 }
						//在对应名字后面再加上属性值
						result.add(nextNode.get(j).getProperty(property));
				}
			}
		return result;
	}
	public ArrayList<Object> isRoleName(ArrayList<Node> nextNode, ArrayList<String> wordList, int i){
		ArrayList<Object> list = new ArrayList<Object>();
		nextNode = findNodeByLabel(Labels.ROLE);
		for(Node node: nextNode){
				if(node.getProperty("name").equals(wordList.get(i))){
				//System.out.println(node.getProperty("name"));
						nextNode.clear();
						nextNode.add(node);
				//涉及到具体某一个角色的问题与两种，第一种，问该角色简介，直接回去迭代
						//第二种，后面还有问到其演员
						if(wordList.get(i + 1).equals("演员")){
							//System.out.println("ark for actor");
								Iterator<Relationship> relativeActor = node.getRelationships(MyRelationshipTypes.ACTOR_BY).iterator();
										if(relativeActor.hasNext()){
													Node actor =relativeActor.next().getOtherNode(node);
													//System.out.println(actor.getProperty("name"));
													if(actor.getProperty("name").equals(node.getProperty("actor"))){
														//System.out.println("equal");
														nextNode.clear();
														nextNode.add(actor);
														i ++;
														break;
													}
												}
											}		
										}							
									}
		
		list.add(nextNode);
		list.add(i);
		return list;	
	}
	public void getTheme(){
		this.graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File("/home/yingying/下载/neo4jMovieGraph/但丁密码"));
		try(Transaction tx = graphDB.beginTx()){
				ArrayList<Node> nextNode = findNodeByLabel(Labels.MOVIE);//用来不断地承接结点，直到问句的最后一个结 
				Node movie = nextNode.get(0);
				TraversalDescription td = this.graphDB.traversalDescription()
						.breadthFirst()
						.relationships(MyRelationshipTypes.THEME_IS)
						.evaluator(Evaluators.atDepth(1));
				Traverser tdd = td.traverse(movie);
				for(Path path: tdd){
					System.out.println(path.startNode().toString()+ " -? " + path.length() + "  - "+ path.endNode().toString());
				}
				tx.success();
		}

	}
	
	public ArrayList<Object> searchAnswer(ArrayList<String> wordList){
		/*
		 * wordList 输入的查询语句列表
		 * 数据库指向在这个方法中，而不在构造函数
		 * 最后返回承接答案的链表
		 * */
		ArrayList<Object> result = new ArrayList<Object>();
		String movieName = wordList.get(0);
		//System.out.println("movieName: " + movieName);
		//通过电影名检索到对应的数据库，注意，默认第一项一定是电影名
		String dbPath =  "/home/yingying/下载/neo4jMovieGraph/" + movieName;
		//System.out.println(dbPath);
		this.graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File(dbPath));
		try(Transaction tx = this.graphDB.beginTx()){
			ArrayList<Node> nextNode = findNodeByLabel(Labels.MOVIE);//用来不断地承接结点，直到问句的最后一个结
			//问句的结尾有两种情况
			//第一种，以结点结束，只有一种问句“电影名，角色列表
			if((wordList.size() == 2) && (wordList.get(1).equals("角色列表"))){
					nextNode = findNodeByLabel(Labels.ROLELIST);
					result.add(nextNode.get(0).getProperty("roleList"));
			}
			else{
			//第二种，最普遍的一种，以属性结尾
				//System.out.println("end with property");
				for(int i = 1; i < wordList.size(); i ++){
				//获取到一个结点，加入链表，注意链表总是不断替换，不会保留之前结点
					if(this.mapNode.containsKey(wordList.get(i))){
						//System.out.println(i + " node is a node !");
						//System.out.println("\n");
						nextNode = findNodeByLabel(this.mapNode.get(wordList.get(i)));
					}else{
						//当跑到一个不表示结点的词的时候
						//当跑到一个不表示结点的词的时候，情况一说明该词表示属性
						if(this.mapProperty.containsKey(wordList.get(i))){
							    //System.out.println(i + " node is a property");
							    result = getProperty(nextNode, wordList, i);											
					  }else{
			         //当跑到一个不表示结点的词的时候，情况二说明其是一个具体确切的角色名称，注意，只考虑询问一个角色名的情况，默认不会询问一个具体公司／编剧／导演的具体信息
						// System.out.println(i +": " + wordList.get(i)+ " node is a role's name");
						  ArrayList<Object> list = isRoleName(nextNode, wordList, i);
						// System.out.println("searchAnswer function " + list.size());
						  nextNode.clear();
						 nextNode = (ArrayList<Node>) list.get(0);
						//System.out.println("now nextNode size is " + nextNode.size() );
						 i = (int) list.get(1);
						// System.out.println("i is " + i);
						}		
					}					
				}				
			}
		}		
		this.graphDB.shutdown();
		return result;
	}
		
	public void printResult(ArrayList<Object> resultList){
		for(Object item : resultList){
			if(item instanceof String[]){
				String[] array = (String[])item;
				if(array.length > 10){
					System.out.println(array[0]);
					System.out.println(array[1]);
					System.out.println(array[2]);
				}else{
					for(String word: array)
						System.out.println(word);
				}
			}else if(item instanceof String){
				System.out.println(item);
			}else{
				System.out.println("impossible !");
			}
		}
	}
	
	public static void main(String[] args) {		
		searchGraph test = new searchGraph();
		ArrayList<String> questionList = new ArrayList<String>();
		questionList.add("美人鱼");
		questionList.add("角色列表");
		questionList.add("角色");
		questionList.add("八哥");
		questionList.add("演员");
		questionList.add("名字");
		ArrayList<Object> resultListFour = test.searchAnswer(questionList);
		System.out.println(resultListFour.size());
//		for(Object item : resultListFour){
//			if(item instanceof String[]){
//				String[] array = (String[])item;
//				if(array.length > 10){
//					System.out.println(array[0]);
//					System.out.println(array[1]);
//					System.out.println(array[2]);
//				}else{
//					for(String word: array)
//						System.out.println(word);
//				}
//			}else if(item instanceof String){
//				System.out.println("string");
//				System.out.println(item);
//			}else{
//				System.out.println("impossible !");
//			}
//		}
		test.printResult(resultListFour);
		//test.getTheme();
/*
		System.out.println("---------------------电影，角色列表，（角色）角色名，属性--------------------------------------------------------");
		ArrayList<String> questionOne = new ArrayList();
		questionOne.add("但丁密码");
		questionOne.add("角色列表");
		questionOne.add("角色");
		questionOne.add("罗伯特·兰登");
		questionOne.add("简介");
		ArrayList<Object> resultListOne = test.searchAnswer(questionOne);
		test.printResult(resultListOne);

		System.out.println("----------------------电影名，角色列表，（角色），　角色名，演员，属性--------------------------------------------------------");		
		ArrayList<String> questionFive = new ArrayList();
		questionFive.add("但丁密码");
		questionFive.add("角色列表");
		questionFive.add("罗伯特·兰登");
		questionFive.add("演员");
		questionFive.add("国籍");
		ArrayList<Object> resultListFive = test.searchAnswer(questionFive);
		test.printResult(resultListFive);

		System.out.println("----------------------------电影，制作，导演列表----------------------------------------------");
		ArrayList<String> questionTwo= new ArrayList();
		questionTwo.add("但丁密码");
		questionTwo.add("制作");
		questionTwo.add("导演列表");
		ArrayList<Object> resultListTwo = test.searchAnswer(questionTwo);
		test.printResult(resultListTwo);

		System.out.println("--------------------------电影，角色列表--------------------------------------------------");
		ArrayList<String> questionThree= new ArrayList();
		questionThree.add("但丁密码");
		questionThree.add("角色列表");
		ArrayList<Object> resultListThree = test.searchAnswer(questionThree);
		test.printResult(resultListThree);
		
		System.out.println("---------------------------电影，票房---------------------------------------------------");		
		ArrayList<String> question= new ArrayList();
		question.add("但丁密码");
		question.add("票房");
		ArrayList<Object> resultList = test.searchAnswer(question);
		test.printResult(resultList);

		System.out.println("-----------------------------电影，制作，出品公司，---出品公司代表作品----------------------------------------------");
		ArrayList<String> question2 = new ArrayList();
		question2.add("但丁密码");
		question2.add("制作");
		question2.add("出品公司");
		question2.add("过去作品");
		ArrayList<Object> resultList2 = test.searchAnswer(question2);
		test.printResult(resultList);*/
		/*	*/

	}
}
