package nearsoft.academy.bigdata.recommendation;

import java.io.*;
import java.util.regex.Pattern;
import java.util.*;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class MovieRecommender {
	public Map products = new HashMap();
	public List<String> productList = new ArrayList<String>();
	public Map users = new HashMap();
	public int elements;
	public String fileName;
	public BufferedReader br = null;
	public StringBuilder textBuilder = new StringBuilder();
	public StringBuilder usersBuilder = new StringBuilder();
	public StringBuilder productsBuilder = new StringBuilder();

	public MovieRecommender(String fileLocation){
		ArrayList<String> list = new ArrayList<String>();
		int counter = 0;
		int element = 0;
		elements = 0;
    	generateFiles(fileLocation);
    	setVariables(fileLocation);
	    
	}

	public void generateFiles(String fileLocation){
		String[] tempLine = new String[2];
	    String[] auxFileLocation = new String[2]; 
		StringBuilder lineBuilder = new StringBuilder();
		try{
			br = new BufferedReader(new FileReader(new File(fileLocation)));
	    	String availalbe;
	    	Map tempProducts = new HashMap();
			Map tempUsers = new HashMap();
			int iUser = 1;
			int iProduct = 1;
	    	while((availalbe = br.readLine()) != null) {
				tempLine = availalbe.split(":");
				if(tempLine[0].contains("productId")){
					if(!tempProducts.containsKey(tempLine[1])){
						tempProducts.put(tempLine[1], iProduct);
			   			lineBuilder.append(iProduct + ",");
			   			productsBuilder.append(tempLine[1].trim() + "\n");
						iProduct++;
					} else{
						lineBuilder.append(tempProducts.get(tempLine[1]) + ",");
					}
				}
			   	else if(tempLine[0].contains("userId")){
			   		if(!tempUsers.containsKey(tempLine[1])){
			   			tempUsers.put(tempLine[1], iUser);
			   			lineBuilder.insert(0,iUser + ",");
			   			usersBuilder.append(tempLine[1].trim() + "\n");
						iUser++;
			   		} else{
			   			lineBuilder.insert(0,tempUsers.get(tempLine[1]) + ",");
			   		}
			   	}
				else if(tempLine[0].contains("score"))
					lineBuilder.append(tempLine[1].trim());
				else if(tempLine[0].contains("text")){
					textBuilder.append(lineBuilder.toString() + "\n");
		    		lineBuilder.setLength(0);
				}
			}
		} catch (FileNotFoundException ei) {
	    	System.out.print("Please include a movie.txt or movie.csv");
    	} catch (IOException ei) {
	    	ei.printStackTrace();
	    } finally {
	    	writeFiles(fileLocation);
	    }
	}


	public void writeFiles(String fileLocation){
		try {
			File file = new File(fileLocation);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(textBuilder.toString());
			bw.close();
			
			file = new File("Products.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			bw.write(productsBuilder.toString());
			bw.close();
			
			file = new File("Users.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			bw.write(usersBuilder.toString());
			bw.close();
			br = new BufferedReader(new FileReader(new File(fileLocation)));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void setVariables(String fileLocation){
		try{
    		String line;
    		Integer productsAmount = 0;
    		Integer usersAmount = 0;
    		br = new BufferedReader(new FileReader(new File("Products.txt")));
    		while((line = br.readLine()) != null) {
	    		productList.add(line);
	    	}
	    	// String[] products = productsBuilder.toString().split(System.getProperty("line.separator"));
	    	// for(String product: products){
	    	// 	productList.add(product);
	    	// }

	    	// String[] usersList = usersBuilder.toString().split(System.getProperty("line.separator"));
	    	// for(String user: usersList){
	    	// 	users.put(user,(++usersAmount));
	    	// }
    		br = new BufferedReader(new FileReader(new File("Users.txt")));
    		while((line = br.readLine()) != null) {
	    		users.put(line,(++usersAmount));
	    	}

	    	br = new BufferedReader(new FileReader(new File(fileLocation)));
    		while((line = br.readLine()) != null) {
	    		elements++;
	    	}
	    	fileName = fileLocation;
	    } catch (IOException e) {
    		e.printStackTrace();
    	}
	}

	public int getTotalReviews(){
		return elements;
	}
	public int getTotalProducts(){
		return productList.size();
	}
	public int getTotalUsers(){
		return users.size();
	}

	public List<String> getRecommendationsForUser(String username){
		List<String> sRecommendations = new ArrayList<String>();
		try{
			int tempID = Integer.parseInt(users.get(username)+"");
			DataModel model = new FileDataModel(new File(fileName));
			UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
			UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
			UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
			
			List<RecommendedItem> recommendations = recommender.recommend(tempID, 3);
			Iterator it = products.entrySet().iterator();
			for (RecommendedItem recommendation : recommendations) {
			    int tempProductID = (int) recommendation.getItemID();
			    sRecommendations.add(productList.get(tempProductID-1));
			}
		} catch (IOException e){

		} catch (TasteException e){
			System.out.println("OH NOOOS");
		}
		return sRecommendations;
	}

}