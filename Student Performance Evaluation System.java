public class Student {
	int StudentID;
	private String Name;
	private double[] marks;
	private double avg;
	public Student(int len) {
		StudentID=0;
		marks= new double[len];
		avg=0;
	}
	public void setStudentID(int ID) {
		StudentID=ID;
	}
	public void setName(String n) {
		Name=n;
	}
	public void setmarks(double[] mark) {
		marks=mark;
	}
	public int getStudentID() {
		return StudentID;
	}
	public String getName() {
		return Name;
	}
	public double[] getmarks() {
		return marks;
	}
	public double calcAvg() {
		for(int i=0;i<marks.length;i++) {
			avg+=marks[i];
		}
		avg/=marks.length;
		//System.out.println("Average Marks:"+avg);
		return avg;
	}
	public String result() {
		double percent=(avg/marks.length)*100;
		if(percent>50) {
			return "Student is Passed";
			
		}
		else {
			return "Student is Fail";
		}
	}
	public void display() {
		System.out.println("Student ID:"+StudentID);
		System.out.println("Student Name:"+Name);
		System.out.print("Marks:");
		for(int i=0;i<marks.length;i++) {
			System.out.print("   "+marks[i]);
		}
		System.out.println();
		System.out.println("Average:"+calcAvg());
		System.out.println("Result:"+result());
	}
	public static void main(String[] args) {
		Student S1=new Student(5);
		S1.setStudentID(100);
		S1.setName("Varad");
		double[] arr={70.7,80.8,90.7,95.0,99.0};
		S1.setmarks(arr);
		S1.display();
	}
	
}
