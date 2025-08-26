package homework.org.app.view;

import homework.org.app.controller.WriterController;
import homework.org.app.model.Writer;

import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class WriterView implements EntityView <Writer> {
    private final WriterController controller;
    private final Scanner scanner;

    public WriterView(WriterController controller, Scanner scanner) {
        this.controller = Objects.requireNonNull(controller);
        this.scanner = Objects.requireNonNull(scanner);
    }

    @Override
    public void showMenu() {
        boolean flag = false;

        while (!flag) {
            System.out.println("\n___ Управление авторами ___");
            System.out.println("1. Создать автора");
            System.out.println("2. Редактировать автора");
            System.out.println("3. Удалить автора");
            System.out.println("4. Показать всех авторов");
            System.out.println("5. Найти автора по ID");
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
        System.out.println("\n___ Создание нового автора ___");
        System.out.println("Введите имя автора: \n");
        String firstName = scanner.nextLine();


        System.out.println("Введите фамилию автора: \n");
        String lastName = scanner.nextLine();

        Writer writer = new Writer();
        writer.setFirstname(firstName);
        writer.setLastname(lastName);

        Writer created = controller.save(writer);
        System.out.println("Создан автор с ID: " + created.getId());
    }

    @Override
    public void edit() {
        System.out.println("\n___ Редактирование автора ___");
        System.out.print("Введите ID автора для редактирования\n");
        Long id = scanner.nextLong();
        scanner.nextLine();

        Writer existing = null;
        try {
            existing = controller.getByID(id);
        } catch (SQLException e) {
            throw new RuntimeException("This id not founded ", e);
        }
        if (existing == null) {
            System.out.println("Автор с таким ID не найден");
            return;
        }

        System.out.println("\n___ Текущие данные ___");
        System.out.println("Имя: " + existing.getFirstname());
        System.out.println("Фамилия: " + existing.getLastname());

        System.out.println("\n___ Введите новые данные (либо оставьте пустым чтобы не изменять ___");
        System.out.print("Новое имя: \n");
        String newFirstName = scanner.nextLine();

        System.out.print("Новая фамилия: ");
        String newLastName = scanner.nextLine();

        if (!newFirstName.isEmpty()) {
            existing.setFirstname(newFirstName);
        }
        if (!newLastName.isEmpty()) {
            existing.setLastname(newLastName);
        }

        try {
            Writer updateWriter = controller.update(existing);
            System.out.println("Данные автора успешно обновлены");
            System.out.println("ID: " + updateWriter.getId());
            System.out.println("Имя: " + updateWriter.getFirstname());
            System.out.println("Фамилия: " + updateWriter.getLastname());
        } catch (Exception e) {
            System.out.println("Ошибка при обновлении: " + e.getMessage());
        }
    }

    @Override
    public void delete() {
        System.out.println("\n___ Удаление автора ___");
        System.out.print("Введите ID автора для удаления\n");
        try {
            Long id = scanner.nextLong();
            scanner.nextLine();

            controller.deleteById(id);
            System.out.println("Автор с ID " + id + " удален");
        } catch (InputMismatchException e) {
            System.out.println("Ошибка: введите корректный числовой ID");
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Ошибка при удалении: " + e.getMessage());
        }
    }

    @Override
    public void showAll() {
        System.out.println("\n___ Показать всех авторов ___");
        List<Writer> list = controller.getAll();

        if (list.isEmpty()) {
            System.out.println("Авторы не найдены");
        }
        for (Writer writer : list) {
            System.out.println("Имя, Фамилия : " + writer.getFirstname() + " " + writer.getLastname());
        }
    }

    @Override
    public void showById() {
        System.out.println("\n___ Поиск автора по ID ___");
        try {
            System.out.print("Введите ID автора для показа\n");
            Long id = scanner.nextLong();
            scanner.nextLine();

            Writer existing = controller.getByID(id);
            if (existing == null) {
                System.out.println("Автор с таким ID не найден");
                return;
            }

            System.out.println("\n___ Текущие данные ___");
            System.out.println("Имя: " + existing.getFirstname());
            System.out.println("Фамилия: " + existing.getLastname());

            if (existing.getPosts() != null && !existing.getPosts().isEmpty()) {
                System.out.println("Количество постов у автора: " + existing.getPosts().size());
            }
        } catch (InputMismatchException e) {
            System.out.println("Ошибка: введите корректный числовой ID");
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Ошибка при поиске: " + e.getMessage());
        }
    }
}
