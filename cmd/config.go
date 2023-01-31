package main

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"errors"
	"fmt"
	"golang.org/x/term"
	"io"
	"os"
)

func main() {
	fmt.Printf("Enter config key: ")
	key, err1 := term.ReadPassword(int(os.Stdin.Fd()))
	if err1 != nil {
		fmt.Println(err1)
		return
	}
	rawConfig, err2 := os.ReadFile("./docker/local/observer/config/observer-config.yml")
	if err2 != nil {
		fmt.Println(err2)
		return
	}
	encConfig, err3 := encrypt(rawConfig, key)
	if err3 != nil {
		fmt.Println(err3)
		return
	}
	err4 := os.WriteFile("./docker/local/observer/config/observer-config.yml.encrypted", encConfig, 0)
	if err4 != nil {
		fmt.Println(err4)
		return
	}
}

func encrypt(plaintext []byte, key []byte) ([]byte, error) {
	c, err := aes.NewCipher(key)
	if err != nil {
		return nil, err
	}

	gcm, err := cipher.NewGCM(c)
	if err != nil {
		return nil, err
	}

	nonce := make([]byte, gcm.NonceSize())
	if _, err = io.ReadFull(rand.Reader, nonce); err != nil {
		return nil, err
	}

	return gcm.Seal(nonce, nonce, plaintext, nil), nil
}

func decrypt(ciphertext []byte, key []byte) ([]byte, error) {
	c, err := aes.NewCipher(key)
	if err != nil {
		return nil, err
	}

	gcm, err := cipher.NewGCM(c)
	if err != nil {
		return nil, err
	}

	nonceSize := gcm.NonceSize()
	if len(ciphertext) < nonceSize {
		return nil, errors.New("ciphertext too short")
	}

	nonce, ciphertext := ciphertext[:nonceSize], ciphertext[nonceSize:]
	return gcm.Open(nil, nonce, ciphertext, nil)
}
