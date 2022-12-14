package ir.saman.socia.model.dao;

import ir.saman.socia.exception.error.AlreadyExistsException;
import ir.saman.socia.exception.error.FileOpException;
import ir.saman.socia.exception.error.NotFoundException;
import ir.saman.socia.model.Entity;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Dao<T extends Entity> {

    private static final String FILES_PATH = "/home/saman/Documents/trash/socia";

    protected Dao() {
    }

    private Collection<T> readFromFile() {
        try {
            File dir = new File(FILES_PATH);
            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(FILES_PATH + "/" + getFileName());
            if (!file.exists()) {
                file.createNewFile();
            }

            if (file.length() == 0) {
                writeToFile(new ArrayList<>());
            }

            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            return (Collection<T>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new FileOpException();
        }
    }

    private void writeToFile(Collection<T> records) {
        try {
            File file = new File(FILES_PATH + "/" + getFileName());
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(records);
        } catch (IOException e) {
            throw new FileOpException();
        }
    }

    abstract String getFileName();

    public T getById(long id) {
        return getAll()
                .stream()
                .filter(item -> item.getId()==id)
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }

    public Collection<T> getAll() {
        return readFromFile();
    }

    public void save(T item) {
        Collection<T> items = getAll();

        items
                .stream()
                .filter(record -> record.equals(item))
                .findAny()
                .ifPresent(user -> {
                    throw new AlreadyExistsException();
                });

        items.add(item);
        writeToFile(items);
    }

    public void update(long id, T item) {
        getById(id);
        delete(id);
        save(item);
    }

    public void delete(long id) {
        List<T> collectionWithoutItem = getAll()
                .stream()
                .filter(user -> user.getId() != id)
                .collect(Collectors.toList());

        writeToFile(collectionWithoutItem);
    }

}
