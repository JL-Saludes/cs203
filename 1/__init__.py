import check50
import check50.java
import re


@check50.check()
def exists():
    """hello.java exists"""
    check50.exists("hello.java")


@check50.check(exists)
def compiles():
    """hello.java compiles"""
    check50.java.compile("hello.java")


@check50.check(compiles)
def john():
    """responds to name John"""
    check_name("John")


@check50.check(compiles)
def lorenz():
    """responds to name Lorenz"""
    check_name("Lorenz")


@check50.check(compiles)
def saludes():
    """responds to name Saludes"""
    check_name("Saludes")


def check_name(name):
    expected = f"hello, {name}\n"
    actual = check50.run("java hello").stdin(name).stdout()

    if not re.match(regex(name), actual):
        try:
            last_character = actual[-1]
        except IndexError:
            raise check50.Mismatch(expected=expected, actual=actual)

        if last_character != "\n":
            raise check50.Mismatch(
                expected=expected,
                actual=actual,
                help="Forgot to print a newline at the end of your output?",
            )
        raise check50.Mismatch(expected=expected, actual=actual)


def regex(string):
    # Fix: comment uses Python syntax
    return f"^[Hh]ello, {re.escape(string)}\n$"
