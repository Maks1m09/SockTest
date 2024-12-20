package by.testSocks.service;


import by.testSocks.entity.Sock;
import by.testSocks.repository.SockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SockService {

    private final SockRepository sockRepository;

    public Sock addIncome(Sock sock) {

        Optional<Sock> existingSockOpt = sockRepository.findById(sock.getId());
        if (existingSockOpt.isPresent()) {
            Sock existingSock = existingSockOpt.get();
            existingSock.setQuantity(existingSock.getQuantity() + sock.getQuantity());
            log.info("Sock {} found and updated", sock);
            return sockRepository.save(existingSock);
        } else {
            log.info("Sock {} not found", sock);
            return sockRepository.save(sock);
        }
    }

    public Sock addOutcome(Sock sock) {
        Sock existingSock = sockRepository.findById(sock.getId())
                .orElseThrow(() -> new IllegalArgumentException("Sock with ID " + sock.getId() + " not found"));

        if (existingSock.getQuantity() < sock.getQuantity()) {
            log.info("Insufficient quantity of sock. Requested: {}, Available: {}", sock.getQuantity(), existingSock.getQuantity());
            throw new IllegalArgumentException("Not enough socks in stock. Available: " + existingSock.getQuantity());
        }
        existingSock.setQuantity(existingSock.getQuantity() - sock.getQuantity());
        log.info("Updated sock: {}. New quantity: {}", existingSock, existingSock.getQuantity());

        return sockRepository.save(existingSock);
    }

    public List<Sock> getFilteredSocks(String color, String operator, Double cottonPercentage, Double minCotton, Double maxCotton) {
        List<Sock> filteredSocks = new ArrayList<>();
        List<Sock> allSocks = sockRepository.findAll();

        for (Sock sock : allSocks) {
            boolean match = true;

            if (color != null && !sock.getColor().equals(color)) {
                match = false;
            }

            if (operator != null) {
                if (operator.equals("moreThan") && sock.getCottonPercentage() <= cottonPercentage) {
                    match = false;
                } else if (operator.equals("lessThan") && sock.getCottonPercentage() >= cottonPercentage) {
                    match = false;
                } else if (operator.equals("equal") && sock.getCottonPercentage() != cottonPercentage) {
                    match = false;
                }
            }

            if (minCotton != null && sock.getCottonPercentage() < minCotton) {
                match = false;
            }
            if (maxCotton != null && sock.getCottonPercentage() > maxCotton) {
                match = false;
            }

            if (match) {
                filteredSocks.add(sock);
            }
        }
        log.info("The filter is exhausted");
        return filteredSocks;
    }

    public Sock updateSock(Long id, Sock newSockData) {
        Sock existingSock = sockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sock not found"));
        existingSock.setColor(newSockData.getColor());
        existingSock.setCottonPercentage(newSockData.getCottonPercentage());
        existingSock.setQuantity(newSockData.getQuantity());
        log.info("Sock" + id + "updated");
        return sockRepository.save(existingSock);
    }

    public ResponseEntity<String> handleExcelFile(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            Sock sock = new Sock();
            sock.setColor(row.getCell(0).getStringCellValue());
            sock.setCottonPercentage(row.getCell(1).getNumericCellValue());
            sock.setQuantity((int) row.getCell(2).getNumericCellValue());
            sockRepository.save(sock);
        }
        workbook.close();
        log.info("File uploaded successfully");
        return ResponseEntity.ok("File uploaded successfully");
    }
}
