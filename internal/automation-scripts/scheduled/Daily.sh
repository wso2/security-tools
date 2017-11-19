LOG_HOME="$HOME/outputs"

date=$(date -d "today" +"%Y-%m-%d")
if [ ! -d "$LOG_HOME/$date" ]; then
	mkdir -p $LOG_HOME/$date
fi

timestamp=$(date -d "today" +"%Y-%m-%d-%H.%M.%S")

bash $HOME/scripts/RunDependencyCheck.sh 2>&1 | tee -a $LOG_HOME/$date/daily-scan-dependency-check-$timestamp.log
