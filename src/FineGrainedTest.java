
public class FineGrainedTest {
	public static void main(String args[]){
		FineGrainedSkipList<Integer> mList = new FineGrainedSkipList<Integer>();
		
		for(int i=0; i<10; i++){
			mList.add(i);
		}
		
		for(int i=0; i<10; i++){
			if(mList.contains(i)){
				System.out.println("Adding " + i + " successful");
			}
		}

        for (int i = 0; i < 10; i++) {
            mList.remove(i);
            if (mList.contains(i)) {
                System.out.println("oops fail");
            } else {
                System.out.println(i + " succesfully removed");
            }
        }
	}
}
