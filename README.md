# LZ77 Compression Tool

A Java implementation of the LZ77 lossless compression algorithm with an interactive command-line interface.
This is not intendet for serious this was made as a school project.

## Features

- Lossless file compression and decompression
- Works with any file type
- Performance metrics and compression statistics
- Simple interactive menu system
## Quick Start


**Run:**
```bash
Can be run inside eclipse
```

## Usage

1. **Compress**: Select option 1, enter input/output file paths
2. **Decompress**: Select option 2, enter `.lz77` file path and output path

## Technical Details

- **Window Size**: 8192 bytes
- **Buffer Size**: 255 bytes
- **Token Size**: 4 bytes (distance + length + next byte)
- **Small files**: this program only works for files under 2.1GB.

## Example
```bash
# Compress
Input: myfile.txt
Output: myfile.lz77

# Decompress
Input: myfile.lz77
Output: myfile_restored.txt
```

## Requirements

- Java 8 or higher
