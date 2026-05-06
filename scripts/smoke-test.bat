@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Cool Community Smoke Test
echo ========================================
echo.

set BASE_URL=http://localhost:8082/api
set PASS_COUNT=0
set FAIL_COUNT=0

echo [1/10] Testing service health...
curl -s -o nul -w "%%{http_code}" "%BASE_URL%/post/list?page=1&pageSize=1" > temp.txt 2>nul
set /p STATUS=<temp.txt
if "!STATUS!"=="200" (
    echo     [PASS] Service is running
    set /a PASS_COUNT+=1
) else (
    echo     [FAIL] Service error, status: !STATUS!
    set /a FAIL_COUNT+=1
)

echo.
echo [2/10] Testing post list API...
curl -s -o nul -w "%%{http_code}" "%BASE_URL%/post/list?page=1&pageSize=10" > temp.txt 2>nul
set /p STATUS=<temp.txt
if "!STATUS!"=="200" (
    echo     [PASS] Post list API OK
    set /a PASS_COUNT+=1
) else (
    echo     [FAIL] Post list API error
    set /a FAIL_COUNT+=1
)

echo.
echo [3/10] Testing topic list API...
curl -s -o nul -w "%%{http_code}" "%BASE_URL%/topic/list?page=1&pageSize=10" > temp.txt 2>nul
set /p STATUS=<temp.txt
if "!STATUS!"=="200" (
    echo     [PASS] Topic list API OK
    set /a PASS_COUNT+=1
) else (
    echo     [FAIL] Topic list API error
    set /a FAIL_COUNT+=1
)

echo.
echo [4/10] Testing product list API...
curl -s -o nul -w "%%{http_code}" "%BASE_URL%/product/list?page=1&pageSize=10" > temp.txt 2>nul
set /p STATUS=<temp.txt
if "!STATUS!"=="200" (
    echo     [PASS] Product list API OK
    set /a PASS_COUNT+=1
) else (
    echo     [FAIL] Product list API error
    set /a FAIL_COUNT+=1
)

echo.
echo [5/10] Testing category list API...
curl -s -o nul -w "%%{http_code}" "%BASE_URL%/category/list" > temp.txt 2>nul
set /p STATUS=<temp.txt
if "!STATUS!"=="200" (
    echo     [PASS] Category list API OK
    set /a PASS_COUNT+=1
) else (
    echo     [FAIL] Category list API error
    set /a FAIL_COUNT+=1
)

echo.
echo [6/10] Testing search API...
curl -s -o nul -w "%%{http_code}" "%BASE_URL%/search/all?keyword=test" > temp.txt 2>nul
set /p STATUS=<temp.txt
if "!STATUS!"=="200" (
    echo     [PASS] Search API OK
    set /a PASS_COUNT+=1
) else (
    echo     [FAIL] Search API error
    set /a FAIL_COUNT+=1
)

echo.
echo [7/10] Testing user register validation...
curl -s -o nul -w "%%{http_code}" -X POST -H "Content-Type: application/json" -d "{}" "%BASE_URL%/user/register" > temp.txt 2>nul
set /p STATUS=<temp.txt
if not "!STATUS!"=="200" (
    echo     [PASS] Register validation OK
    set /a PASS_COUNT+=1
) else (
    echo     [FAIL] Register validation error
    set /a FAIL_COUNT+=1
)

echo.
echo [8/10] Testing user login error handling...
curl -s -o nul -w "%%{http_code}" -X POST -H "Content-Type: application/json" -d "{\"username\":\"test\",\"password\":\"wrong\"}" "%BASE_URL%/user/login" > temp.txt 2>nul
set /p STATUS=<temp.txt
if not "!STATUS!"=="200" (
    echo     [PASS] Login error handling OK
    set /a PASS_COUNT+=1
) else (
    echo     [FAIL] Login error handling error
    set /a FAIL_COUNT+=1
)

echo.
echo [9/10] Testing post not found handling...
curl -s -o nul -w "%%{http_code}" "%BASE_URL%/post/detail/999999999" > temp.txt 2>nul
set /p STATUS=<temp.txt
if not "!STATUS!"=="200" (
    echo     [PASS] Post not found handling OK
    set /a PASS_COUNT+=1
) else (
    echo     [FAIL] Post not found handling error
    set /a FAIL_COUNT+=1
)

echo.
echo [10/10] Testing search empty keyword handling...
curl -s -o nul -w "%%{http_code}" "%BASE_URL%/search/all?keyword=" > temp.txt 2>nul
set /p STATUS=<temp.txt
if not "!STATUS!"=="200" (
    echo     [PASS] Empty keyword handling OK
    set /a PASS_COUNT+=1
) else (
    echo     [FAIL] Empty keyword handling error
    set /a FAIL_COUNT+=1
)

del temp.txt 2>nul

echo.
echo ========================================
echo   Test Results Summary
echo ========================================
echo   Passed: %PASS_COUNT%
echo   Failed: %FAIL_COUNT%
echo   Total:  10
echo ========================================

if %FAIL_COUNT%==0 (
    echo   Status: [PASS] Smoke test passed
    exit /b 0
) else (
    echo   Status: [FAIL] Smoke test failed
    exit /b 1
)
