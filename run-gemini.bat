@echo off
:: 현재 배포 파일이 있는 경로를 기준으로 환경 변수 설정
SET "GEMINI_HOME=%~dp0.gemini"
SET "XDG_CONFIG_HOME=%~dp0.gemini"

:: .gemini 폴더가 없다면 생성
if not exist "%~dp0.gemini" mkdir "%~dp0.gemini"

echo Gemini CLI를 SSD 모드로 실행합니다...
echo 저장소 위치: %GEMINI_HOME%

:: 실제 gemini 명령어 실행 (전달받은 인자값 포함)
gemini %*