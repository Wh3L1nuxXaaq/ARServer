package ARS.event;

import ARS.security.AuthorizeTable;

public class UpdateEvent {
    public static void update() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

                for (String ip : AuthorizeTable.getAuthorizeTable().keySet()) {
                    Integer time = AuthorizeTable.getAuthorizeTimeByIp(ip);
                    if (time != null) {
                        time -= 5;
                        System.out.println("[GUARD] Authorize -5s by event");

                        if (time <= 0) {
                            System.out.println("[GUARD] Authorize stopped by event");
                            AuthorizeTable.removeAuthorizeTimeByIp(ip);
                        } else {
                            AuthorizeTable.addAuthorizeTimeByIp(ip, time);
                        }
                    }
                }
            }
        }).start();
    }
}
