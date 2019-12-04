class HandleFuture<Integer> implements Callable<Integer> {
	
	private Integer num;
	
	public HandleFuture(Integer num) {
		this.num = num;
	}
 
	@Override
	public Integer call() throws Exception {
		Thread.sleep(3*100);
		System.out.println(Thread.currentThread().getName());
		return num;
	}
	
}