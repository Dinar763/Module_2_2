package homework.org.app.view;

import homework.org.app.controller.PostController;
import homework.org.app.controller.WriterController;
import homework.org.app.model.Post;
import homework.org.app.model.Writer;

import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class PostView implements EntityView<Post>{
    private final PostController postController;
    private final WriterController writerController;
    private final Scanner scanner;

    public PostView(PostController postController, WriterController writerController, Scanner scanner) {
        this.postController = Objects.requireNonNull(postController);
        this.writerController = Objects.requireNonNull(writerController);
        this.scanner = Objects.requireNonNull(scanner);
    }

    @Override
    public void showMenu() {
        boolean flag = false;

        while (!flag) {
            System.out.println("\n___ Управление постами ___");
            System.out.println("1. Создать пост");
            System.out.println("2. Редактировать пост");
            System.out.println("3. Удалить пост");
            System.out.println("4. Показать все посты");
            System.out.println("5. Найти пост по ID");
            System.out.println("0. Назад");
            System.out.print("Ваш выбор: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> create();
                    case 2 -> edit();
                    case 3 -> delete();
                    case 4 -> showAll();
                    case 5 -> showById();
                    case 0 -> flag = true;
                    default -> System.out.println("Неверный ввод!");
                }
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: введите число");
                scanner.nextLine();
            }
        }
    }

    @Override
    public void create() {
        System.out.println("\n___ Создание нового поста ___");
        System.out.println("Введите имя и фамилию автора\n");

        String[] fio = scanner.nextLine().split(" ");
        if (fio.length != 2) {
            System.out.println("Ошибка, нужно ввести имя и фамилию автора через пробел");
            return;
        }
        Writer writer = writerController.findOrCreate(fio[0], fio[1]);

        System.out.println("Введите название поста: \n");
        String str = scanner.nextLine();


        Post post = new Post();
        post.setContent(str);
        post.setWriter(writer);

        try {
            Post created = postController.save(post);
            System.out.println("Создан пост с ID: " + created.getId());
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }


    }

    @Override
    public void edit() {
        System.out.println("\n___ Редактирование поста ___");
        System.out.print("Введите ID поста для редактирования\n");
        Long id = scanner.nextLong();
        scanner.nextLine();

        Post existing = null;
        try {
            existing = postController.getByID(id);
        } catch (SQLException e) {
            throw new RuntimeException("This id not founded ", e);
        }
        if (existing == null) {
            System.out.println("Пост с таким ID не найден");
            return;
        }

        System.out.println("\n___ Текущие данные ___");
        System.out.println("Название: " + existing.getContent());

        System.out.println("\n___ Введите новые данные (либо оставьте пустым чтобы не изменять ___");
        System.out.print("Новое название: \n");
        String newTitle = scanner.nextLine();


        if (!newTitle.isEmpty()) {
            existing.setContent(newTitle);
        }


        try {
            Post updatePost = postController.update(existing);
            System.out.println("Данные поста успешно обновлены");
            System.out.println("ID: " + updatePost.getId());
        } catch (Exception e) {
            System.out.println("Ошибка при обновлении: " + e.getMessage());
        }
    }

    @Override
    public void delete() {
        System.out.println("\n___ Удаление поста ___");
        System.out.print("Введите ID поста для удаления\n");
        try {
            Long id = scanner.nextLong();
            scanner.nextLine();

            postController.deleteById(id);
            System.out.println("Пост с ID " + id + " помечен как удаленный");
        } catch (InputMismatchException e) {
            System.out.println("Ошибка: введите корректный числовой ID\n");
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Ошибка при удалении: " + e.getMessage());
        }
    }

    @Override
    public void showAll() {
        System.out.println("\n___ Список всех постов ___");
        List<Post> list = postController.getAll();

        if (list.isEmpty()) {
            System.out.println("Посты не найдены");
        }


        for (Post post : list) {
            System.out.println("Описание: " + post.getContent());
        }
    }

    @Override
    public void showById() {
        System.out.println("\n___ Поиск поста по ID ___");
        try {
            System.out.print("Введите ID поста для показа\n");
            Long id = scanner.nextLong();
            scanner.nextLine();

            Post existing = postController.getByID(id);
            if (existing == null) {
                System.out.println("Пост с таким ID не найден");
                return;
            }

            System.out.println("\n___ Текущие данные ___");
            System.out.println("Описание: " + existing.getContent());
            System.out.println("Статус: " + existing.getStatus().getDisplayName());

            if (existing.getLabels() != null && !existing.getLabels().isEmpty()) {
                System.out.println("Количество labels: " + existing.getLabels().size());
            } else {
                System.out.println("Labels: нет");
            }
        } catch (InputMismatchException e) {
            System.out.println("Ошибка: введите корректный числовой ID");
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Ошибка при поиске: " + e.getMessage());
        }
    }

}
