# .bash_profile

# Get the aliases and functions
if [ -f ~/.bashrc ]; then
	. ~/.bashrc
fi

# User specific environment and startup programs

PATH=$PATH:$HOME/ .local/bni:$HOME/bin

export PATH

echo "WELCOME JICHUN! The current time is: "

date 
 
