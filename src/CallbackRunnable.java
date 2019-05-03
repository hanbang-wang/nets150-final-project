class CallbackRunnable extends Thread {
    Runnable callback;

    CallbackRunnable(Runnable a) {
        super(a);
    }

    @Override
    public void run() {
        super.run();
        callback.run();
    }
}
