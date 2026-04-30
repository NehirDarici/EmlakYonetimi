package com.emlak.datastructures;

import com.emlak.models.Property;

import java.util.ArrayList;
import java.util.List;

public class KDTree {

    // Ağacın düğüm (Node) yapısı
    private class Node {
        Property property;
        Node left, right;

        Node(Property property) {
            this.property = property;
            this.left = null;
            this.right = null;
        }
    }

    private Node root;

    public KDTree() {
        this.root = null;
    }

    // Dışarıdan çağrılan ekleme (Insert) metodu
    public void insert(Property property) {
        root = insertRec(root, property, 0);
    }

    // Özyinelemeli (Recursive) ekleme metodu
    private Node insertRec(Node root, Property property, int depth) {
        if (root == null) {
            return new Node(property);
        }

        // K=2 olduğu için derinlik 0'da X'e, 1'de Y'ye bakıyoruz (depth % 2)
        int cd = depth % 2;

        if (cd == 0) { // X eksenine göre kıyaslama
            if (property.getX() < root.property.getX())
                root.left = insertRec(root.left, property, depth + 1);
            else
                root.right = insertRec(root.right, property, depth + 1);
        } else { // Y eksenine göre kıyasla
            if (property.getY() < root.property.getY())
                root.left = insertRec(root.left, property, depth + 1);
            else
                root.right = insertRec(root.right, property, depth + 1);
        }

        return root;
    }

    // Dışarıdan çağrılan mekansal arama (Range Search) metodu
    // Belirtilen (targetX, targetY) merkezine 'radius' kadar uzaklıktaki evleri bulur
    public List<Property> findInRadius(double targetX, double targetY, double radius) {
        List<Property> results = new ArrayList<>();
        searchRec(root, targetX, targetY, radius, 0, results);
        return results;
    }

    // Özyinelemeli arama metodu (Ağaç dallarını budayarak ilerler)
    private void searchRec(Node root, double targetX, double targetY, double radius, int depth, List<Property> results) {
        if (root == null) return;

        // Öklid Uzaklığı (Euclidean Distance) Formülü
        double dx = root.property.getX() - targetX;
        double dy = root.property.getY() - targetY;
        double distance = Math.sqrt((dx * dx) + (dy * dy));

        // Eğer ev, belirlediğimiz yarıçap (radius) içindeyse listeye ekle
        if (distance <= radius) {
            results.add(root.property);
        }

        int cd = depth % 2;

        // Ağacın sağına mı soluna mı gideceğimize veya budama (pruning) yapıp yapmayacağımıza karar veriyoruz
        if (cd == 0) { // X ekseni
            if (targetX - radius < root.property.getX()) {
                searchRec(root.left, targetX, targetY, radius, depth + 1, results);
            }
            if (targetX + radius > root.property.getX()) {
                searchRec(root.right, targetX, targetY, radius, depth + 1, results);
            }
        } else { // Y ekseni
            if (targetY - radius < root.property.getY()) {
                searchRec(root.left, targetX, targetY, radius, depth + 1, results);
            }
            if (targetY + radius > root.property.getY()) {
                searchRec(root.right, targetX, targetY, radius, depth + 1, results);
            }
        }
    }
}
