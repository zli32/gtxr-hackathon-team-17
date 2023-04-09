# ChatPCB - Augmented Reality PCB Prototyping Tool
Team ChatPCB's innovative mobile AR app is the result of our shared passion for electronics and our determination to create a tool that empowers DIY enthusiasts. 
By leveraging Google's ARCore and speech recognition and our deep understanding of PCB design, our app transforms the student 
hobbyist PCB fabrication process, reducing assembly time and enhancing board accuracy.

## Motivation
Currently, student and hobbyist hand assembly of PCBs is a tedious and sometimes frustrating process. To place each part, an individual must reference a bill of materials (BOM), a long and complicated CSV table containing information correlating the reference designators on the PCB (ex. R1, R2) with the correct part number that needs to be placed at that position. This requires a computer at hand, which is often obstructive. Further, the context switch needed at every placement to check the CSV table signficantly slows the user down as the user hunts for the correct CSV entry and then looks back at the board and tries to find the position of the part. 

## Solution
ChatPCB simplifies the process of finding the correct part for a given position and does the work of remembering that position for you. With a board render and a board XML file, both easily and commonly obtained files from PCB design tools, the tool is able to find and track the correct PCB within its camera viewfinder. With a simple verbal command speaking out the reference designator (ex. R1), ChatPCB accurately places a virtual AR marker at the position of the part and brings up the part number and other relevant information needed to find the part. Once the part is found, the user can simply look back at the AR marker and immediately locate the correct position once again. This removes the mental effort required for the context switch and also clears up the workspace, resulting in a signifcant improvement in PCB assembly experince along with a noticeable reduction in assembly time. 

## Tutorial
1. At splash screen:
    - use the choose image button to upload the board render image
    - use the choose xml button to upload the board xml file
2. Hit Continue to AR button to enter the AR screen.
3. Place the PCB in the viewfinder, and slowly move the camera around the PCB. The AR finder will calibrate and pick up the PCB.
    - The AR finder is orientation independent. The AR finder is capable, in the correct conditions, of finding the PCB in any orientation.
4. Say a reference designator (such as R1, R2, etc.). The app continuously listens for your verbal input. If the reference designator is valid, an AR marker will be dropped at the correct position on the PCB. Information needed to locate the correct part will be displayed on screen.
    - As the phone moves, the AR application keeps tracking the PCB, ensuring the marker remains in the correct location.
5. When ready to move on to the next placement, just say the next reference designator, and the marker will move to the next position. 