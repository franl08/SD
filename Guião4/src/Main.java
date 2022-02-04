class Main {
    public static void main(String[] args) {
        Barrier b = new Barrier(3);

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("Vou fazer await");
                b.await();
            } catch (Exception e) {
            }
            System.out.println("await retornou");
            try {
                Thread.sleep(2000);
                System.out.println("Vou fazer await");
                b.await();
            } catch (Exception e) {
            }
            System.out.println("await retornou");
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("Vou fazer await");
                b.await();
            } catch (Exception e) {
            }
            System.out.println("await retornou");
            try {
                Thread.sleep(1000);
                System.out.println("Vou fazer await");
                b.await();
            } catch (Exception e) {
            }
            System.out.println("await retornou");
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("Vou fazer await");
                b.await();
            } catch (Exception e) {
            }
            System.out.println("await retornou");
            try {
                Thread.sleep(3000);
                System.out.println("Vou fazer await");
                b.await();
            } catch (Exception e) {
            }
            System.out.println("await retornou");
        }).start();
    }
}