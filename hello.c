#include <stdio.h>

int main() {
    char name[100];

    printf("What is your name? ");
    scanf("%99s", name);

    printf("Hello, %s\n", name);

    return 0;
}
