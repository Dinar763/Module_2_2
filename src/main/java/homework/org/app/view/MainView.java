package homework.org.app.view;

import homework.org.app.controller.LabelController;
import homework.org.app.controller.PostController;
import homework.org.app.controller.WriterController;
import homework.org.app.controller.impl.LabelControllerImpl;
import homework.org.app.controller.impl.PostControllerImpl;
import homework.org.app.controller.impl.WriterControllerImpl;
import homework.org.app.repository.LabelRepository;
import homework.org.app.repository.PostRepository;
import homework.org.app.repository.WriterRepository;
import homework.org.app.repository.mysql.MySqlLabelRepository;
import homework.org.app.repository.mysql.MySqlPostRepository;
import homework.org.app.repository.mysql.MySqlWriterRepository;
import homework.org.app.service.LabelService;
import homework.org.app.service.PostService;
import homework.org.app.service.WriterService;
import homework.org.app.service.impl.LabelServiceImpl;
import homework.org.app.service.impl.PostServiceImpl;
import homework.org.app.service.impl.WriterServiceImpl;
import homework.org.app.util.ConnectionPoolManager;

import java.util.InputMismatchException;
import java.util.Scanner;

public class MainView {
    private final Scanner scanner;
    private final WriterView writerView;
    private final PostView postView;
    private final LabelView labelView;
    ConnectionPoolManager connectionManager = ConnectionPoolManager.getInstance();

    public MainView() {
        this.scanner = new Scanner(System.in);
        WriterRepository writerRepository = new MySqlWriterRepository(connectionManager);
        PostRepository postRepository = new MySqlPostRepository(connectionManager);
        LabelRepository labelRepository = new MySqlLabelRepository(connectionManager);

        WriterService writerService = new WriterServiceImpl(writerRepository);
        PostService postService = new PostServiceImpl(postRepository);
        LabelService labelService = new LabelServiceImpl(labelRepository);

        WriterController writerController = new WriterControllerImpl(writerService);
        PostController postController = new PostControllerImpl(postService);
        LabelController labelController = new LabelControllerImpl(labelService);

        this.writerView = new WriterView(writerController, scanner);
        this.postView = new PostView(postController, writerController, scanner);
        this.labelView = new LabelView(labelController, scanner);
    }

    public void start() {
        boolean flag = false;

        while (!flag) {
            System.out.println("\n+++ Главное меню +++");
            System.out.println("1. Работа с постами");
            System.out.println("2. Работа с авторами");
            System.out.println("3. Работа с лейблами");
            System.out.println("0. Выход");
            System.out.print("Сделайте выбор: \n");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> postView.showMenu();
                    case 2 -> writerView.showMenu();
                    case 3 -> labelView.showMenu();
                    case 0 -> flag = true;
                }
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: введите число");
                scanner.nextLine();
            }
        }
        System.out.println("Программа завершена");
        scanner.close();
    }
}
