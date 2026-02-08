package com.fdm.group.Etrm_Project_Prototype;

public class ResizeableArray {
	
	private int[] array;
	
	private int size;
	
	
	public ResizeableArray(int size) {
		//default size
		array = new int[10];
		this.size = size;
	}
	
	
	public void addValue(int number) {
		
		array[size] = number;
	}
	
	public void removeAtIndex(int index) {
		
		if(index >size || index<0) {
			
			System.out.println("index must be within the size of the array");
			
		}
		else {
			//move everything over
			for(int i=index; i<size-1; i++) {
				
				array[i] = array[i+1];
			}
			size--;
		}
		
	}
	
	
	public void resizeArray() {
		
		
	}

}
