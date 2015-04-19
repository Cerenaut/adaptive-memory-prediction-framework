# adaptive-memory-prediction-framework
The Memory-Prediction Framework (MPF) has been widely applied to unsupervised learning problems, for both classification and prediction. However, so far there has been no attempt to incorporate MPF/HTM in reinforcement learning or other adaptive systems; that is, to use knowledge embodied within the hierarchy to control a system, or to generate behaviour for an agent. This problem is interesting because the human neocortex is believed to play a vital role in the generation of behaviour, and the MPF is a model of the human neocortex.

We propose some simple and biologically-plausible enhancements to the Memory-Prediction Framework. These cause it to explore and interact with an external world, while trying to maximize a continuous, time-varying reward function. All behaviour is generated and controlled within the MPF hierarchy. The homogeneity (self-sameness) of the MPF hierarchy is preserved, i.e. all processing units are identical. The hierarchy develops from a random initial configuration by interaction with the world and reinforcement learning only. Among other demonstrations, we show that a 2-node hierarchy can learn to successfully play “rocks, paper, scissors” against a predictable opponent.

This project is designed to support a research paper published in PLOS ONE. The paper can be found here:

http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0029264

PDF:

http://www.plosone.org/article/fetchObjectAttachment.action?uri=info%3Adoi%2F10.1371%2Fjournal.pone.0029264&representation=PDF

All code is written in Java. Full source code for a Kohonen-SOM MPF implementation is provided, along with extensions for adaptive behaviour generation and several demonstration programs. The paper & code also demonstrate variable-order prediction of future states within the SOM-based MPF.



Mozilla Public License 1.1
https://www.mozilla.org/MPL/
